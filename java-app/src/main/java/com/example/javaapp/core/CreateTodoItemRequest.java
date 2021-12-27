package com.example.javaapp.core;

import lombok.Value;

@Value
public class CreateTodoItemRequest {
    String name;
    State state;
}
