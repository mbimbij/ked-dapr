package com.example.javaapp.core;

import com.example.javaapp.core.events.DomainEvent;
import reactor.core.publisher.Mono;

public interface IPublishStateChange {
    Mono<Void> publish(DomainEvent domainEvent);
}
