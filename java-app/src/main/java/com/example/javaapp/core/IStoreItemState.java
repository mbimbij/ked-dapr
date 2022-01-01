package com.example.javaapp.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IStoreItemState {
    Mono<TodoItem> createItem(NewTodoItem todoItem);

    Mono<TodoItem> getById(int id);

    Flux<TodoItem> getAll();

    Mono<TodoItem> updateItem(TodoItem updatedTodoItem);

    Mono<TodoItem> deleteById(int id);
}
