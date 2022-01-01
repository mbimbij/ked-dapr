package com.example.javaapp.inputadapter;

import com.example.javaapp.core.IGetThingsDone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class AppRestController {

    @Autowired
    private IGetThingsDone iGetThingsDone;

    @PostMapping(path = "/items", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<TodoItemResponseDto> createItem(@RequestBody CreateItemRequestDto createItemDto) {
        return iGetThingsDone.createItem(createItemDto.toDomainObject()).map(TodoItemResponseDto::new);
    }

    @GetMapping(path = "/items/{id}", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<TodoItemResponseDto> getById(@PathVariable int id) {
        return iGetThingsDone.getById(id).map(TodoItemResponseDto::new);
    }

    @GetMapping(path = "/items", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<TodoItemResponseDto> getAll() {
        return iGetThingsDone.getAll().map(TodoItemResponseDto::new);
    }

    @PutMapping(path = "/items", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<TodoItemResponseDto> update(@RequestBody UpdateItemRequestDto updateItemRequestDto) {
        return iGetThingsDone.updateItem(updateItemRequestDto.toDomainObject()).map(TodoItemResponseDto::new);
    }

    @DeleteMapping(path = "/items/{id}", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<Void> deleteById(@PathVariable int id) {
        return iGetThingsDone.deleteById(id);
    }
}
