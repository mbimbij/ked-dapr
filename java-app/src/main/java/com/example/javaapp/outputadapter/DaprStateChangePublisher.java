package com.example.javaapp.outputadapter;

import com.example.javaapp.core.IPublishStateChange;
import com.example.javaapp.core.events.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class DaprStateChangePublisher implements IPublishStateChange {
    private WebClient webClient;

    public DaprStateChangePublisher(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Void> publish(DomainEvent domainEvent) {
        log.info("publishing {}", domainEvent.toString());
        return Mono.empty().then();
    }
}
