package com.example.javaapp.core;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class TodoApp implements IGetThingsDone {
    private final IInvokeOtherService iInvokeOtherService;
    private final IStoreItemState iStoreItemState;
    private final INotifyItemStateChange iNotifyItemStateChange;

    @Override
    public void createItem(TodoItem todoItem) {

    }

    @Override
    public Optional<TodoItem> getById(int id) {
        return Optional.empty();
    }

    @Override
    public List<TodoItem> getAll() {
        return null;
    }

    @Override
    public TodoItem updateItem(TodoItem updatedTodoItem) {
        return null;
    }

    @Override
    public void deleteById(int id) {

    }
}
