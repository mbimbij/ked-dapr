package com.example.javaapp.core.events;

import lombok.Value;

@Value
public class ItemDeletedEvent implements DomainEvent {
    int itemId;
}
