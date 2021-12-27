package com.example.javaapp.core;

import reactor.core.publisher.Mono;

public interface IInvokeOtherService {
    Mono<String> getOtherValue();
}
