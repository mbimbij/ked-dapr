package com.example.javaapp.inputadapter;

import com.example.javaapp.core.CreateTodoItemRequest;
import com.example.javaapp.core.State;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class CreateItemRequestDto {
    String name;
    State state;

    CreateTodoItemRequest toDomainObject() {
        return new CreateTodoItemRequest(name, state);
    }
}
