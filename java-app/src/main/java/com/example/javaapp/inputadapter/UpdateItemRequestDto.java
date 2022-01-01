package com.example.javaapp.inputadapter;

import com.example.javaapp.core.State;
import com.example.javaapp.core.UpdateTodoItemRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class UpdateItemRequestDto {
    int id;
    String name;
    State state;

    UpdateTodoItemRequest toDomainObject() {
        return new UpdateTodoItemRequest(id, name, state);
    }
}
