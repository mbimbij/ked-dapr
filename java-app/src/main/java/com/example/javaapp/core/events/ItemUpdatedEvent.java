package com.example.javaapp.core.events;

import com.example.javaapp.core.TodoItem;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class ItemUpdatedEvent implements DomainEvent {
    private TodoItem todoItem;

    public ItemUpdatedEvent(TodoItem todoItem) {
        this.todoItem = todoItem;
    }
}
