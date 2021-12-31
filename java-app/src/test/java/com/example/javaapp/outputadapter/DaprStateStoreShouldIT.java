package com.example.javaapp.outputadapter;

import com.example.javaapp.JavaAppApplication;
import com.example.javaapp.core.IStoreItemState;
import com.example.javaapp.core.NewTodoItem;
import com.example.javaapp.core.State;
import com.example.javaapp.core.TodoItem;
import com.example.javaapp.outputadapter.DaprStateStore.BindingToItemMapper;
import com.example.javaapp.outputadapter.DaprStateStore.DaprItem;
import com.example.javaapp.outputadapter.DaprStateStore.DaprSaveItemRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.example.javaapp.outputadapter.DaprStateStore.BINDING_URI_FORMAT;
import static com.example.javaapp.outputadapter.DaprStateStore.STATESTORE_KEY_URI_FORMAT;
import static com.example.javaapp.outputadapter.DaprStateStore.STATESTORE_URI_FORMAT;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {JavaAppApplication.class})
@Slf4j
class DaprStateStoreShouldIT {

    private final int itemId1 = 777;
    private final int itemId2 = 778;
    @Autowired
    private IStoreItemState daprStateStore;
    @Autowired
    private WebClient webClient;
    @Value("${dapr.statestore.name}")
    String statestoreName;
    @Value("${dapr.binding.name}")
    String bindingName;
    private String name;
    private final State state = State.TODO;
    private final String otherValue = "someOtherValue";

    @BeforeEach
    void setUp() {
        name = "someItem-" + UUID.randomUUID();
        deleteAll();
    }

    @AfterEach
    void tearDown() {
        deleteAll();
    }

    private void deleteAll() {
        String uri = BINDING_URI_FORMAT.formatted(bindingName);
        webClient.post()
                 .uri(uri)
                 .contentType(MediaType.APPLICATION_JSON)
                 .bodyValue("{\"operation\":\"exec\",\"metadata\":{\"sql\":\"DELETE FROM dapr_ked.state\"}}")
                 .retrieve()
                 .onStatus(HttpStatus::isError, ClientResponse::createException)
                 .toBodilessEntity()
                 .doOnError(throwable -> log.error("%s\n%s".formatted(
                         throwable.getMessage(),
                         ((InternalServerError) throwable).getResponseBodyAsString(StandardCharsets.UTF_8))))
                 .block(Duration.ofSeconds(5));
    }

    @Test
    void createItem() {
        // GIVEN
        NewTodoItem newItem = new NewTodoItem(name, state, otherValue);
        TodoItem expectedItem = new TodoItem(itemId2, name, state, otherValue);

        // WHEN
        Mono<TodoItem> createItemResponseMono = daprStateStore.createItem(newItem);

        // THEN
        Mono<TodoItem> itemMono = createItemResponseMono.flatMap(
                todoItem -> {
                    String uri = STATESTORE_KEY_URI_FORMAT.formatted(statestoreName, todoItem.getId());
                    return webClient.get()
                                    .uri(uri)
                                    .retrieve()
                                    .bodyToMono(DaprItem.class)
                                    .map(daprItem -> daprItem.toTodoItem(todoItem.getId()));
                });

        StepVerifier.create(itemMono)
                    .assertNext(item -> assertThat(item).usingRecursiveComparison()
                                                        .ignoringFields("id")
                                                        .isEqualTo(expectedItem))
                    .verifyComplete();
    }

    @Test
    void retrieveAPreviouslyCreatedItem() {
        // GIVEN
        TodoItem expectedItem = new TodoItem(itemId2, name, state, otherValue);

        List<DaprSaveItemRequest> saveItemRequest = List.of(DaprSaveItemRequest.fromTodoItem(itemId2, new NewTodoItem(name, state, otherValue)));
        Mono<ResponseEntity<Void>> responseEntityMono = webClient.post()
                                                                 .uri(STATESTORE_URI_FORMAT.formatted(statestoreName))
                                                                 .bodyValue(saveItemRequest)
                                                                 .retrieve()
                                                                 .toEntity(Void.class);

        // WHEN
        Mono<TodoItem> itemMono = responseEntityMono.then(daprStateStore.getById(itemId2));

        // THEN
        StepVerifier.create(itemMono)
                    .assertNext(item -> assertThat(item).usingRecursiveComparison()
                                                        .ignoringFields("id")
                                                        .isEqualTo(expectedItem))
                    .verifyComplete();
    }

    @Test
    void updateAPreviouslyCreatedItem() {
        // GIVEN
        NewTodoItem newItem = new NewTodoItem(name, state, otherValue);
        String updatedName = "updatedName";
        State updatedState = State.DOING;
        String updatedOtherValue = "updatedOtherValue";
        Mono<TodoItem> createResponse = daprStateStore.createItem(newItem);

        TodoItem expectedUpdatedItem = new TodoItem(itemId2, updatedName, updatedState, updatedOtherValue);
        TodoItem updatedItem = new TodoItem(itemId2, updatedName, updatedState, updatedOtherValue);

        // WHEN
        Mono<TodoItem> updateResponse = createResponse.then(daprStateStore.updateItem(updatedItem));

        // THEN
        Mono<TodoItem> getUpdatedItem = updateResponse.flatMap(
                todoItem -> webClient.get()
                                     .uri(STATESTORE_KEY_URI_FORMAT.formatted(statestoreName, todoItem.getId()))
                                     .retrieve()
                                     .bodyToMono(DaprItem.class)
                                     .map(daprItem -> daprItem.toTodoItem(todoItem.getId())));

        StepVerifier.create(getUpdatedItem)
                    .assertNext(item -> assertThat(item).usingRecursiveComparison()
                                                        .ignoringFields("id")
                                                        .isEqualTo(expectedUpdatedItem))
                    .verifyComplete();
    }

    @Test
    void deleteAPreviouslyCreatedItem() {
        // GIVEN
        NewTodoItem newItem = new NewTodoItem(name, state, otherValue);
        Mono<TodoItem> createResponse = daprStateStore.createItem(newItem);

        // WHEN
        Mono<TodoItem> deleteResponse = createResponse.flatMap(todoItem -> daprStateStore.deleteById(todoItem.getId())
                                                                                         .thenReturn(todoItem));

        // THEN
        Mono<TodoItem> getDeletedItem = deleteResponse.flatMap(
                todoItem -> webClient.get()
                                     .uri(STATESTORE_KEY_URI_FORMAT.formatted(statestoreName, todoItem.getId()))
                                     .retrieve()
                                     .bodyToMono(DaprItem.class)
                                     .map(daprItem -> daprItem.toTodoItem(todoItem.getId())));

        StepVerifier.create(getDeletedItem).verifyComplete();
    }

    @Test
    void returnAnEmptyFlux_whenGetAllItem_onEmptyBase() {
        // GIVEN
        String uri = STATESTORE_URI_FORMAT.formatted(statestoreName);

        // WHEN
        Flux<TodoItem> allItems = daprStateStore.getAll();

        // THEN
        StepVerifier.create(allItems)
                    .verifyComplete();
    }

    @Test
    void returnAFluxOfItems_whenGetAllItem_onNonEmptyBase() {
        // GIVEN
        String uri = STATESTORE_URI_FORMAT.formatted(statestoreName);
        DaprSaveItemRequest item1 = new DaprSaveItemRequest(String.valueOf(itemId1), new DaprItem("name1", State.TODO.toString(), otherValue));
        DaprSaveItemRequest item2 = new DaprSaveItemRequest(String.valueOf(itemId2), new DaprItem("name2", State.TODO.toString(), otherValue));
        Mono<Void> createResponse = webClient.post()
                                             .uri(uri)
                                             .bodyValue(List.of(item1, item2))
                                             .retrieve()
                                             .bodyToMono(Void.class);

        // WHEN
        Flux<TodoItem> getAllResponse = createResponse.thenMany(daprStateStore.getAll());

        // THEN
        StepVerifier.create(getAllResponse)
                    .assertNext(todoItem -> assertThat(todoItem).isEqualTo(item1.toTodoItem()))
                    .assertNext(todoItem -> assertThat(todoItem).isEqualTo(item2.toTodoItem()))
                    .verifyComplete();
    }

    @SneakyThrows
    @Test
    void mapBindingResponseCorrectly() {
        // GIVEN
        String bindingResponseString = """
                {
                  "eTag": "08835823-efcc-4a96-9b00-614a665e25ba",
                  "id": "plop||2",
                  "insertDate": "2021-12-27T08:19:00Z",
                  "isbinary": 0,
                  "updateDate": "2021-12-27T08:19:00Z",
                  "value": "eyJuYW1lIjogInNvbWVJdGVtLTBhNDU5NDNjLTYyM2ItNDI4NC1hNzFlLWRjNTRhM2E3NTA5ZiIsICJzdGF0ZSI6ICJUT0RPIiwgIm90aGVyVmFsdWUiOiAic29tZU90aGVyVmFsdWUifQ=="
                }
                """;
        TodoItem expectedTodoItem = new TodoItem(2, "someItem-0a45943c-623b-4284-a71e-dc54a3a7509f", State.TODO, "someOtherValue");

        // WHEN
        Map<String, Object> bindingResponse = new ObjectMapper().readValue(bindingResponseString, new TypeReference<>() {
        });
        TodoItem todoItem = new BindingToItemMapper().mapResponse(bindingResponse);

        // THEN
        assertThat(todoItem).isEqualTo(expectedTodoItem);
    }
}