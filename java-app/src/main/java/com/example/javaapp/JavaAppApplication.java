package com.example.javaapp;

import com.example.javaapp.core.IGetThingsDone;
import com.example.javaapp.core.IInvokeOtherService;
import com.example.javaapp.core.IPublishStateChange;
import com.example.javaapp.core.IStoreItemState;
import com.example.javaapp.core.TodoApp;
import com.example.javaapp.outputadapter.DaprOtherServiceInvoker;
import com.example.javaapp.outputadapter.DaprStateChangePublisher;
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
                                         IPublishStateChange iPublishStateChange) {
        return new TodoApp(iInvokeOtherService, iStoreItemState, iPublishStateChange);
    }

    @Bean
    public IInvokeOtherService daprOtherServiceInvoker(WebClient webClient) {
        return new DaprOtherServiceInvoker(webClient);
    }

    @Bean
    public IStoreItemState daprStateStore(WebClient webClient,
                                          @Value("${dapr.statestore.name}") String statestoreName,
                                          @Value("${dapr.binding.name}") String bindingName) {
        return new DaprStateStore(webClient, statestoreName, bindingName);
    }

    @Bean
    public IPublishStateChange daprItemStateChangeNotifier(WebClient webClient) {
        return new DaprStateChangePublisher(webClient);
    }

    @Bean
    public WebClient webClient(@Value("${dapr.port}") int daprPort) {
        String daprSidecarUrl = String.format("http://localhost:%d", daprPort);
        return WebClient.builder()
                        .baseUrl(daprSidecarUrl)
                        .build();
    }
}
