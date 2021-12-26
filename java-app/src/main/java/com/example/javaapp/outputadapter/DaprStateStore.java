package com.example.javaapp.outputadapter;

import com.example.javaapp.core.IStoreItemState;
import com.example.javaapp.core.NewTodoItem;
import com.example.javaapp.core.State;
import com.example.javaapp.core.TodoItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class DaprStateStore implements IStoreItemState {
    private final WebClient webClient;
    private static int nextItemId = 0;

    public DaprStateStore(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<TodoItem> createItem(NewTodoItem todoItem) {
        int itemId = nextItemId++;
        DaprSaveItemRequest createItemRequest = DaprSaveItemRequest.fromTodoItem(itemId, todoItem);
//        return webClient.post()
//                        .uri("/v1.0/state/statestore")
//                        .bodyValue(List.of(createItemRequest))
//                        .retrieve()
//                        .bodyToMono(Void.class)
//                        .then(
//                                webClient.get()
//                                       .uri("/v1.0/state/statestore/" + createItemRequest.getKey())
//                                       .retrieve()
//                                       .bodyToMono(DaprItem.class)
//                                       .map(daprItem -> daprItem.toTodoItem(itemId)));
        return webClient.post()
                        .uri("/v1.0/state/statestore")
                        .bodyValue(List.of(createItemRequest))
                        .retrieve()
                        .toEntity(Void.class)
                        .map(responseEntity -> createItemRequest.toTodoItem());
    }

    @Override
    public Mono<TodoItem> getById(int id) {
        return webClient.get()
                        .uri("/v1.0/state/statestore/" + id)
                        .retrieve()
                        .bodyToMono(DaprItem.class)
                        .map(daprItem -> daprItem.toTodoItem(id));
    }

    @Override
    public Flux<TodoItem> getAll() {
        return null;
    }

    @Override
    public Mono<TodoItem> updateItem(TodoItem updatedTodoItem) {
        return null;
    }

    @Override
    public Mono<Void> deleteById(int id) {
        return null;
    }

    @Value
    public static class DaprSaveItemRequest {
        String key;
        DaprItem value;

        static DaprSaveItemRequest fromTodoItem(int itemId, NewTodoItem todoItem) {
            return new DaprSaveItemRequest(Integer.toString(itemId),
                                           new DaprItem(todoItem.getName(),
                                                        todoItem.getState().toString(),
                                                        todoItem.getOtherValue()));
        }

        TodoItem toTodoItem() {
            return new TodoItem(Integer.parseInt(key),
                                value.getName(),
                                State.valueOf(value.getState()),
                                value.getOtherValue());
        }
    }

    @NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
    @AllArgsConstructor
    @Value
    public static class DaprItem {
        String name;
        String state;
        String otherValue;

        TodoItem toTodoItem(int id) {
            return new TodoItem(id, name, State.valueOf(state), otherValue);
        }
    }
}
