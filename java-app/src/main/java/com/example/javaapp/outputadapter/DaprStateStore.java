package com.example.javaapp.outputadapter;

import com.example.javaapp.core.IStoreItemState;
import com.example.javaapp.core.NewTodoItem;
import com.example.javaapp.core.State;
import com.example.javaapp.core.TodoItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;
import java.util.Map;

public class DaprStateStore implements IStoreItemState {
    private static int nextItemId = 0;
    private final WebClient webClient;
    private final String statestoreName;
    private final String bindingName;
    private final BindingToItemMapper bindingToItemMapper;

    public static final String STATESTORE_URI_FORMAT = "/v1.0/state/%s";
    public static final String STATESTORE_KEY_URI_FORMAT = "/v1.0/state/%s/%d";
    public static final String BINDING_URI_FORMAT = "/v1.0/bindings/%s";

    public DaprStateStore(WebClient webClient, String statestoreName, String bindingName) {
        this.webClient = webClient;
        this.statestoreName = statestoreName;
        this.bindingName = bindingName;
        bindingToItemMapper = new BindingToItemMapper();
    }

    @Override
    public Mono<TodoItem> createItem(NewTodoItem todoItem) {
        int itemId = nextItemId++;
        DaprSaveItemRequest createItemRequest = DaprSaveItemRequest.fromTodoItem(itemId, todoItem);
        String uri = STATESTORE_URI_FORMAT.formatted(statestoreName);
        return webClient.post()
                        .uri(uri)
                        .bodyValue(List.of(createItemRequest))
                        .retrieve()
                        .onStatus(HttpStatus::isError, ClientResponse::createException)
                        .bodyToMono(Void.class)
                        .thenReturn(createItemRequest.toTodoItem());
    }

    @Override
    public Mono<TodoItem> getById(int id) {
        String uri = STATESTORE_KEY_URI_FORMAT.formatted(statestoreName, id);
        return webClient.get()
                        .uri(uri)
                        .retrieve()
                        .onStatus(HttpStatus::isError, ClientResponse::createException)
                        .bodyToMono(DaprItem.class)
                        .map(daprItem -> daprItem.toTodoItem(id));
    }

    @Override
    public Flux<TodoItem> getAll() {
        String uri = BINDING_URI_FORMAT.formatted(bindingName);
        // @formatter:off
        return webClient.post()
                        .uri(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue("{\"operation\":\"query\",\"metadata\":{\"sql\":\"SELECT * FROM dapr_ked.state\"}}")
                        .retrieve()
                        .onStatus(HttpStatus::isError, ClientResponse::createException)
                        .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                        .map(bindingToItemMapper::mapResponse);
        // @formatter:on
    }

    @Override
    public Mono<TodoItem> updateItem(TodoItem updatedTodoItem) {
        DaprSaveItemRequest updatedItem = DaprSaveItemRequest.fromTodoItem(updatedTodoItem);
        String uri = STATESTORE_URI_FORMAT.formatted(statestoreName);
        return webClient.post()
                        .uri(uri)
                        .bodyValue(List.of(updatedItem))
                        .retrieve()
                        .onStatus(HttpStatus::isError, ClientResponse::createException)
                        .bodyToMono(Void.class)
                        .thenReturn(updatedItem.toTodoItem());
    }

    @Override
    public Mono<Void> deleteById(int id) {
        String uri = STATESTORE_KEY_URI_FORMAT.formatted(statestoreName, id);
        return webClient.delete()
                        .uri(uri)
                        .retrieve()
                        .onStatus(HttpStatus::isError, ClientResponse::createException)
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

    public static class BindingToItemMapper {
        ObjectMapper objectMapper = new ObjectMapper();

        @SneakyThrows
        public TodoItem mapResponse(Map<String, Object> bindingOutput) {
            String id = bindingOutput.get("id").toString().split("\\|\\|")[1];
            String valueBase64 = bindingOutput.get("value").toString();
            Map<String, String> value = objectMapper.readValue(Base64.getDecoder().decode(valueBase64), new TypeReference<>() {
            });
            return new TodoItem(Integer.parseInt(id), value.get("name"), State.valueOf(value.get("state")), value.get("otherValue"));
        }
    }
}
