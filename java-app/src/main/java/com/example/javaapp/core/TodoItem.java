package com.example.javaapp.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@AllArgsConstructor
@Builder(toBuilder = true)
public class TodoItem {
    int id;
    String name;
    State state;
    String otherValue;
}
