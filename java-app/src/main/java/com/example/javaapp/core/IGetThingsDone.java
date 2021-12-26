package com.example.javaapp.core;

import java.util.List;
import java.util.Optional;

public interface IGetThingsDone {
    void createItem(TodoItem todoItem);
    Optional<TodoItem> getById(int id);
    List<TodoItem> getAll();
    TodoItem updateItem(TodoItem updatedTodoItem);
    void deleteById(int id);
}
