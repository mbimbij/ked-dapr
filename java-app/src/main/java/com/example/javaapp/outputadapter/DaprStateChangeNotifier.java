package com.example.javaapp.outputadapter;

import com.example.javaapp.core.INotifyStateChange;
import org.springframework.web.reactive.function.client.WebClient;

public class DaprStateChangeNotifier implements INotifyStateChange {
    public DaprStateChangeNotifier(WebClient webClient) {

    }
}
