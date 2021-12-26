package com.example.javaapp.outputadapter;

import com.example.javaapp.core.INotifyItemStateChange;
import org.springframework.web.reactive.function.client.WebClient;

public class DaprItemStateChangeNotifier implements INotifyItemStateChange {
    public DaprItemStateChangeNotifier(WebClient webClient) {

    }
}
