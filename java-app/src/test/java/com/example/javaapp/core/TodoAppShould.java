package com.example.javaapp.core;

import com.example.javaapp.core.events.ItemCreatedEvent;
import com.example.javaapp.core.events.ItemDeletedEvent;
import com.example.javaapp.utils.TestUtil;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    private IStoreItemState iStoreItemState;
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
        TodoItem todoItem = new TodoItem(itemId, name, state, otherValue);
        doReturn(Mono.just(todoItem)).when(iStoreItemState).getById(anyInt());

        // WHEN
        todoApp.deleteById(itemId).block();
        ItemDeletedEvent expectedEvent = new ItemDeletedEvent(itemId);

        // THEN
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThatCode(() -> verify(iInvokeOtherService, never()).getOtherValue()).doesNotThrowAnyException();
            softAssertions.assertThatCode(() -> verify(iStoreItemState).getById(eq(itemId))).doesNotThrowAnyException();
            softAssertions.assertThatCode(() -> verify(iPublishStateChange)
                    .publish(TestUtil.argThat(domainEvent -> assertThat(domainEvent).isEqualTo(expectedEvent)))).doesNotThrowAnyException();
        });
    }

    @Test
    void notDeleteItem_whenItemDoesNotExist() {
        // GIVEN
        TodoItem todoItem = new TodoItem(itemId, name, state, otherValue);
        doReturn(Mono.empty()).when(iStoreItemState).getById(anyInt());

        // WHEN
        todoApp.deleteById(itemId).block();
        ItemDeletedEvent expectedEvent = new ItemDeletedEvent(itemId);

        // THEN
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThatCode(() -> verify(iInvokeOtherService, never()).getOtherValue()).doesNotThrowAnyException();
            softAssertions.assertThatCode(() -> verify(iStoreItemState).getById(eq(itemId))).doesNotThrowAnyException();
            softAssertions.assertThatCode(() -> verify(iPublishStateChange, never()).publish(any())).doesNotThrowAnyException();
        });
    }

    private static class StateStoreMock implements IStoreItemState {
        private final int itemId;

        private StateStoreMock(int itemId) {
            this.itemId = itemId;
        }

        @Override
        public Mono<TodoItem> createItem(NewTodoItem todoItem) {
            return Mono.just(new TodoItem(itemId, todoItem.getName(), todoItem.getState(), todoItem.getOtherValue()));
        }

        @Override
        public Mono<TodoItem> getById(int id) {
            return null;
        }

        @Override
        public Flux<TodoItem> getAll() {
            return null;
        }

        @Override
        public Mono<TodoItem> updateItem(TodoItem updatedTodoItem) {
            return null;
        }

        @Override
        public Mono<Void> deleteById(int id) {
            return null;
        }
    }
}