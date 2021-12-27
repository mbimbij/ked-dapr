package com.example.javaapp.core.events;

import com.example.javaapp.core.TodoItem;
import lombok.Value;

@Value
public class ItemCreatedEvent implements DomainEvent {
    TodoItem item;
}
