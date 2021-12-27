package com.example.javaapp.outputadapter;

import com.example.javaapp.JavaAppApplication;
import com.example.javaapp.core.IStoreItemState;
import com.example.javaapp.core.NewTodoItem;
import com.example.javaapp.core.State;
import com.example.javaapp.core.TodoItem;
import com.example.javaapp.outputadapter.DaprStateStore.DaprItem;
import com.example.javaapp.outputadapter.DaprStateStore.DaprSaveItemRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {JavaAppApplication.class})
class DaprStateStoreShouldIT {

    private final int itemId = 777;
    @Autowired
    private IStoreItemState daprStateStore;
    @Autowired
    private WebClient webClient;
    private String name;
    private final State state = State.TODO;
    private final String otherValue = "someOtherValue";

    @BeforeEach
    void setUp() {
        name = "someItem-" + UUID.randomUUID();
        Mono<TodoItem> cleanedItem = webClient.delete()
                                              .uri("/v1.0/state/statestore/" + itemId)
                                              .retrieve()
                                              .bodyToMono(Void.class)
                                              .then(webClient.get()
                                                             .uri("/v1.0/state/statestore/" + itemId)
                                                             .retrieve()
                                                             .bodyToMono(DaprItem.class)
                                                             .map(daprItem -> daprItem.toTodoItem(itemId)));
        StepVerifier.create(cleanedItem)
                    .verifyComplete();
        System.out.println();
    }

    @Test
    void createItem() {
        // GIVEN
        NewTodoItem newItem = new NewTodoItem(name, state, otherValue);
        TodoItem expectedItem = new TodoItem(itemId, name, state, otherValue);

        // WHEN
        Mono<TodoItem> createItemResponseMono = daprStateStore.createItem(newItem);

        // THEN
        Mono<TodoItem> itemMono = createItemResponseMono.flatMap(
                todoItem -> webClient.get()
                                     .uri("/v1.0/state/statestore/" + todoItem.getId())
                                     .retrieve()
                                     .bodyToMono(DaprItem.class)
                                     .map(daprItem -> daprItem.toTodoItem(todoItem.getId())));

        StepVerifier.create(itemMono)
                    .assertNext(item -> assertThat(item).usingRecursiveComparison()
                                                        .ignoringFields("id")
                                                        .isEqualTo(expectedItem))
                    .verifyComplete();
    }

    @Test
    void retrieveAPreviouslyCreatedItem() {
        // GIVEN
        TodoItem expectedItem = new TodoItem(itemId, name, state, otherValue);

        List<DaprSaveItemRequest> saveItemRequest = List.of(DaprSaveItemRequest.fromTodoItem(itemId, new NewTodoItem(name, state, otherValue)));
        Mono<ResponseEntity<Void>> responseEntityMono = webClient.post()
                                                                 .uri("/v1.0/state/statestore")
                                                                 .bodyValue(saveItemRequest)
                                                                 .retrieve()
                                                                 .toEntity(Void.class);

        // WHEN
        Mono<TodoItem> itemMono = responseEntityMono.then(daprStateStore.getById(itemId));

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

        TodoItem expectedUpdatedItem = new TodoItem(itemId, updatedName, updatedState, updatedOtherValue);
        TodoItem updatedItem = new TodoItem(itemId, updatedName, updatedState, updatedOtherValue);

        // WHEN
        Mono<TodoItem> updateResponse = createResponse.then(daprStateStore.updateItem(updatedItem));

        // THEN
        Mono<TodoItem> getUpdatedItem = updateResponse.flatMap(
                todoItem -> webClient.get()
                                     .uri("/v1.0/state/statestore/" + todoItem.getId())
                                     .retrieve()
                                     .bodyToMono(DaprItem.class)
                                     .map(daprItem -> daprItem.toTodoItem(todoItem.getId())));

        StepVerifier.create(getUpdatedItem)
                    .assertNext(item -> assertThat(item).usingRecursiveComparison()
                                                        .ignoringFields("id")
                                                        .isEqualTo(expectedUpdatedItem))
                    .verifyComplete();
    }
}