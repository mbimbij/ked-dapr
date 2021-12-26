package com.example.javaapp.outputadapter;

import com.example.javaapp.core.IStoreItemState;
import org.springframework.web.reactive.function.client.WebClient;

public class DaprStateStore implements IStoreItemState {
    public DaprStateStore(WebClient webClient) {

    }
}
