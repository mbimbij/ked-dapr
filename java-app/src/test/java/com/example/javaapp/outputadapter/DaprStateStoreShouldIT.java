package com.example.javaapp.outputadapter;

import com.example.javaapp.JavaAppApplication;
import com.example.javaapp.core.IStoreItemState;
import com.example.javaapp.core.NewTodoItem;
import com.example.javaapp.core.State;
import com.example.javaapp.core.TodoItem;
import com.example.javaapp.outputadapter.DaprStateStore.DaprItem;
import com.example.javaapp.outputadapter.DaprStateStore.DaprSaveItemRequest;
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

    @Autowired
    private IStoreItemState daprStateStore;

    @Autowired
    private WebClient webClient;

    @Test
    void createItem() {
        // GIVEN
        NewTodoItem newItem = new NewTodoItem("someItem", State.TODO, "someOtherValue");
        TodoItem expectedItem = new TodoItem(777, newItem.getName(), newItem.getState(), newItem.getOtherValue());

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
        int itemId = 777;
        String name = "someItem-" + UUID.randomUUID();
        State state = State.TODO;
        String otherValue = "someOtherValue";

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
}