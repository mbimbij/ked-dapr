package com.example.javaapp.outputadapter;

import com.example.javaapp.core.IPublishStateChange;
import com.example.javaapp.core.events.DomainEvent;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class DaprStateChangePublisher implements IPublishStateChange {
    private WebClient webClient;

    public DaprStateChangePublisher(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Void> publish(DomainEvent domainEvent) {
        return Mono.empty();
    }
}
