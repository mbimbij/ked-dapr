package com.example.javaapp;

import com.example.javaapp.core.IGetThingsDone;
import com.example.javaapp.core.IInvokeOtherService;
import com.example.javaapp.core.INotifyItemStateChange;
import com.example.javaapp.core.IStoreItemState;
import com.example.javaapp.core.TodoApp;
import com.example.javaapp.outputadapter.DaprItemStateChangeNotifier;
import com.example.javaapp.outputadapter.DaprOtherServiceInvoker;
import com.example.javaapp.outputadapter.DaprStateStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class JavaAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaAppApplication.class, args);
    }

    @Bean
    public IGetThingsDone iGetThingsDone(IInvokeOtherService iInvokeOtherService,
                                         IStoreItemState iStoreItemState,
                                         INotifyItemStateChange iNotifyItemStateChange) {
        return new TodoApp(iInvokeOtherService, iStoreItemState, iNotifyItemStateChange);
    }

    @Bean
    public IInvokeOtherService daprOtherServiceInvoker(WebClient webClient) {
        return new DaprOtherServiceInvoker(webClient);
    }

    @Bean
    public IStoreItemState daprStateStore(WebClient webClient) {
        return new DaprStateStore(webClient);
    }

    @Bean
    public INotifyItemStateChange daprItemStateChangeNotifier(WebClient webClient) {
        return new DaprItemStateChangeNotifier(webClient);
    }

    @Bean
    public WebClient webClient(@Value("${dapr.port}") int daprPort) {
        String daprSidecarUrl = String.format("http://localhost:%d", daprPort);
        return WebClient.builder()
                 .baseUrl(daprSidecarUrl)
                 .build();
    }
}
