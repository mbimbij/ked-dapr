package com.example.javaapp.core;

import com.example.javaapp.core.events.ItemCreatedEvent;
import com.example.javaapp.core.events.ItemDeletedEvent;
import com.example.javaapp.core.events.ItemUpdatedEvent;
import com.example.javaapp.utils.TestUtil;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static com.example.javaapp.core.State.DOING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

class TodoAppShould {
    private final int itemId = 777;
    private final String name = "someName";
    private final State state = State.TODO;
    private final String otherValue = "someOtherValue";
    private IInvokeOtherService iInvokeOtherService;
    private StateStoreMock iStoreItemState;
    private IPublishStateChange iPublishStateChange;

    @BeforeEach
    void setUp() {
        iInvokeOtherService = mock(IInvokeOtherService.class);
        doReturn(Mono.just(otherValue)).when(iInvokeOtherService).getOtherValue();

        iStoreItemState = spy(new StateStoreMock(itemId));

        iPublishStateChange = mock(IPublishStateChange.class);
        doReturn(Mono.empty()).when(iPublishStateChange).publish(any());

        todoApp = new TodoApp(iInvokeOtherService, iStoreItemState, iPublishStateChange);
    }

    private TodoApp todoApp;

    @Test
    void invokeTheOtherService_andSaveState_andPublishEvent_whenCreateItem() {
        // WHEN
        todoApp.createItem(new CreateTodoItemRequest(name, state)).block();
        TodoItem expectedItem = new TodoItem(itemId, name, state, otherValue);
        ItemCreatedEvent expectedEvent = new ItemCreatedEvent(expectedItem);

        // THEN
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThatCode(() -> verify(iInvokeOtherService).getOtherValue()).doesNotThrowAnyException();
            softAssertions.assertThatCode(() -> verify(iStoreItemState).createItem(any())).doesNotThrowAnyException();
            softAssertions.assertThatCode(() -> verify(iPublishStateChange)
                    .publish(TestUtil.argThat(domainEvent -> assertThat(domainEvent).isEqualTo(expectedEvent)))).doesNotThrowAnyException();
        });
    }

    @Test
    void deleteItem_whenItemExists() {
        // GIVEN
        iStoreItemState.createItem(new NewTodoItem(name, state, otherValue));

        // WHEN
        todoApp.deleteById(itemId).block();
        ItemDeletedEvent expectedEvent = new ItemDeletedEvent(itemId);

        // THEN
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThatCode(() -> verify(iInvokeOtherService, never()).getOtherValue()).doesNotThrowAnyException();
            softAssertions.assertThatCode(() -> verify(iStoreItemState).deleteById(eq(itemId))).doesNotThrowAnyException();
            softAssertions.assertThatCode(() -> verify(iPublishStateChange).publish(eq(expectedEvent))).doesNotThrowAnyException();
        });
    }

    @Test
    void notDeleteItem_whenItemDoesNotExist() {
        // GIVEN
        doReturn(Mono.empty()).when(iStoreItemState).getById(anyInt());

        // WHEN
        todoApp.deleteById(itemId).block();
        ItemDeletedEvent expectedEvent = new ItemDeletedEvent(itemId);

        // THEN
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThatCode(() -> verify(iInvokeOtherService, never()).getOtherValue()).doesNotThrowAnyException();
            softAssertions.assertThatCode(() -> verify(iStoreItemState).deleteById(eq(itemId))).doesNotThrowAnyException();
            softAssertions.assertThatCode(() -> verify(iPublishStateChange, never()).publish(any())).doesNotThrowAnyException();
        });
    }

    @Test
    void sendItemUpdatedEvent_whenUpdateFromTodoToDoing() {
        // GIVEN
        TodoItem previousItemState = new TodoItem(itemId, name, state, otherValue);
        doReturn(Mono.just(previousItemState)).when(iStoreItemState).getById(anyInt());
        UpdateTodoItemRequest updateTodoItemRequest = new UpdateTodoItemRequest(itemId, name, DOING);
        TodoItem newItem = new TodoItem(itemId, name, DOING, otherValue);

        // WHEN
        todoApp.updateItem(updateTodoItemRequest).block();
        ItemUpdatedEvent expectedEvent = new ItemUpdatedEvent(newItem);

        // THEN
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThatCode(() -> verify(iInvokeOtherService).getOtherValue()).doesNotThrowAnyException();
            softAssertions.assertThatCode(() -> verify(iStoreItemState).updateItem(eq(newItem))).doesNotThrowAnyException();
            softAssertions.assertThatCode(() -> verify(iPublishStateChange)
                    .publish(TestUtil.argThat(domainEvent -> assertThat(domainEvent).isEqualTo(expectedEvent)))).doesNotThrowAnyException();
        });
    }

    @Test
    void getAllItems() {
        // GIVEN
        CreateTodoItemRequest createTodoItemRequest1 = new CreateTodoItemRequest("plop", state);
        CreateTodoItemRequest createTodoItemRequest2 = new CreateTodoItemRequest("other", state);
        todoApp.createItem(createTodoItemRequest1)
               .doOnSuccess(todoItem -> iStoreItemState.setNextItemId(778))
               .then(todoApp.createItem(createTodoItemRequest2))
               .block();
        TodoItem expectedItem1 = new TodoItem(0, "plop", state, otherValue);
        TodoItem expectedItem2 = new TodoItem(0, "other", state, otherValue);

        // WHEN
        Flux<TodoItem> allItems = todoApp.getAll();

        // THEN
        StepVerifier.create(allItems)
                    .assertNext(todoItem -> assertThat(todoItem).usingRecursiveComparison().ignoringFields("id").isEqualTo(expectedItem1))
                    .assertNext(todoItem -> assertThat(todoItem).usingRecursiveComparison().ignoringFields("id").isEqualTo(expectedItem2))
                    .verifyComplete();
    }

    private static class StateStoreMock implements IStoreItemState {
        private int nextItemId;
        private Map<Integer, TodoItem> items = new HashMap<>();

        private StateStoreMock(int nextItemId) {
            this.nextItemId = nextItemId;
        }

        @Override
        public Mono<TodoItem> createItem(NewTodoItem todoItem) {
            TodoItem item = new TodoItem(nextItemId, todoItem.getName(), todoItem.getState(), todoItem.getOtherValue());
            items.put(item.getId(), item);
            return Mono.just(item);
        }

        @Override
        public Mono<TodoItem> getById(int id) {
            return Mono.just(items.get(id));
        }

        @Override
        public Flux<TodoItem> getAll() {
            return Flux.fromIterable(items.values());
        }

        @Override
        public Mono<TodoItem> updateItem(TodoItem updatedTodoItem) {
            items.put(updatedTodoItem.getId(), updatedTodoItem);
            return Mono.just(updatedTodoItem);
        }

        @Override
        public Mono<TodoItem> deleteById(int id) {
            TodoItem todoItem = items.get(id);
            items.remove(id);
            return Mono.justOrEmpty(todoItem);
//            if(todoItem == null){
//                return Mono.empty();
//            }else{
//                return Mono.just(todoItem);
//            }
        }

        public void setNextItemId(int nextItemId) {
            this.nextItemId = nextItemId;
        }
    }
}