package com.example.javaapp.inputadapter;

import com.example.javaapp.core.State;
import com.example.javaapp.core.TodoItem;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class TodoItemResponseDto {
    int id;
    String name;
    State state;
    String otherValue;

    TodoItemResponseDto(TodoItem todoItem) {
        id = todoItem.getId();
        name = todoItem.getName();
        state = todoItem.getState();
        otherValue = todoItem.getOtherValue();
    }
}
