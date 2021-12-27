package com.example.javaapp.outputadapter;

import com.example.javaapp.core.IInvokeOtherService;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

public class DaprOtherServiceInvoker implements IInvokeOtherService {
    private WebClient webClient;

    public DaprOtherServiceInvoker(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<String> getOtherValue() {
        return Mono.just("some other value");
    }
}
