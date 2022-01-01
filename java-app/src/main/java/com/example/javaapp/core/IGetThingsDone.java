package com.example.javaapp.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGetThingsDone {
    Mono<TodoItem> createItem(CreateTodoItemRequest todoItem);

    Mono<TodoItem> getById(int id);

    Flux<TodoItem> getAll();

    Mono<TodoItem> updateItem(UpdateTodoItemRequest updatedTodoItem);

    Mono<Void> deleteById(int id);
}
