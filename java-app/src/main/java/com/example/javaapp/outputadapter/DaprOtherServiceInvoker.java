package com.example.javaapp.outputadapter;

import com.example.javaapp.core.IInvokeOtherService;
import org.springframework.web.reactive.function.client.WebClient;

public class DaprOtherServiceInvoker implements IInvokeOtherService {
    public DaprOtherServiceInvoker(WebClient webClient) {

    }
}
