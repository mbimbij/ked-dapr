package com.example.javaapp.core;

import lombok.Value;

@Value
public class UpdateTodoItemRequest {
    int id;
    String name;
    State state;
}
