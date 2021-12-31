package com.example.javaapp.core;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface IGetThingsDone {
    Mono<TodoItem> createItem(CreateTodoItemRequest todoItem);

    Optional<TodoItem> getById(int id);

    List<TodoItem> getAll();

    TodoItem updateItem(TodoItem updatedTodoItem);

    Mono<Void> deleteById(int id);
}
