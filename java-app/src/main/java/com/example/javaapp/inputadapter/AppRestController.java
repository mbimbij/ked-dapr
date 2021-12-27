package com.example.javaapp.inputadapter;

import com.example.javaapp.core.IGetThingsDone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AppRestController {

    @Autowired
    private IGetThingsDone iGetThingsDone;

    @PostMapping(path = "/items", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Mono<CreateItemResponseDto> createItem(@RequestBody CreateItemRequestDto createItemDto) {
        return iGetThingsDone.createItem(createItemDto.toDto()).map(CreateItemResponseDto::new);
    }
}
