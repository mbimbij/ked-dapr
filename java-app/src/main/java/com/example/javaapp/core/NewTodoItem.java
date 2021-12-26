package com.example.javaapp.core;

import lombok.Value;

@Value
public class NewTodoItem {
    String name;
    State state;
    String otherValue;
}
