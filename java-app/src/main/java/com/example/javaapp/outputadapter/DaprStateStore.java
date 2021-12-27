package com.example.javaapp.outputadapter;

import com.example.javaapp.core.IStoreItemState;
import com.example.javaapp.core.NewTodoItem;
import com.example.javaapp.core.State;
import com.example.javaapp.core.TodoItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
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
        return webClient.post()
                        .uri("/v1.0/state/statestore")
                        .bodyValue(List.of(createItemRequest))
                        .retrieve()
                        .bodyToMono(Void.class)
                        .thenReturn(createItemRequest.toTodoItem());
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
        DaprSaveItemRequest updatedItem = DaprSaveItemRequest.fromTodoItem(updatedTodoItem);
        return webClient.post()
                        .uri("/v1.0/state/statestore")
                        .bodyValue(List.of(updatedItem))
                        .retrieve()
                        .bodyToMono(Void.class)
                        .thenReturn(updatedItem.toTodoItem());
    }

    @Override
    public Mono<Void> deleteById(int id) {
        return webClient.delete()
                        .uri("/v1.0/state/statestore/" + id)
                        .retrieve()
                        .bodyToMono(Void.class);
    }

    @Value
    public static class DaprSaveItemRequest {
        String key;
        DaprItem value;

        static DaprSaveItemRequest fromTodoItem(int itemId, NewTodoItem newTodoItem) {
            return new DaprSaveItemRequest(Integer.toString(itemId),
                                           new DaprItem(newTodoItem.getName(),
                                                        newTodoItem.getState().toString(),
                                                        newTodoItem.getOtherValue()));
        }

        static DaprSaveItemRequest fromTodoItem(TodoItem todoItem) {
            return new DaprSaveItemRequest(Integer.toString(todoItem.getId()),
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
