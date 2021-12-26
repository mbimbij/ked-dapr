package com.example.javaapp.core;

import lombok.Value;

@Value
public class TodoItem {
    int id;
    String name;
    State state;
    String otherValue;
}
