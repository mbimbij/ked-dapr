package com.example.javaapp.core;

import com.example.javaapp.core.TodoItem.TodoItemBuilder;
import com.example.javaapp.core.events.ItemCreatedEvent;
import com.example.javaapp.core.events.ItemDeletedEvent;
import com.example.javaapp.core.events.ItemUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class TodoApp implements IGetThingsDone {
    private final IInvokeOtherService iInvokeOtherService;
    private final IStoreItemState iStoreItemState;
    private final IPublishStateChange iPublishStateChange;

    @Override
    public Mono<TodoItem> createItem(CreateTodoItemRequest todoItem) {
        return iInvokeOtherService.getOtherValue()
                                  .map(otherValue -> new NewTodoItem(todoItem.getName(), todoItem.getState(), otherValue))
                                  .flatMap(iStoreItemState::createItem)
                                  .map(ItemCreatedEvent::new)
                                  .zipWhen(iPublishStateChange::publish)
                                  .map(objects -> objects.mapT1(ItemCreatedEvent::getItem))
                                  .map(Tuple2::getT1);
    }

    @Override
    public Optional<TodoItem> getById(int id) {
        return Optional.empty();
    }

    @Override
    public Flux<TodoItem> getAll() {
        return iStoreItemState.getAll();
    }

    @Override
    public Mono<TodoItem> updateItem(TodoItem updatedTodoItem) {
        Mono<TodoItem> updatedItemWithOtherValue = iInvokeOtherService.getOtherValue()
                                                                      .map(updatedTodoItem.toBuilder()::otherValue)
                                                                      .map(TodoItemBuilder::build);

//        iStoreItemState.getById(updatedTodoItem.getId())
//                       .zipWith(updatedItemWithOtherValue)
//                       .doOnSuccess(objects -> iStoreItemState.updateItem(objects.getT2()))
//                       .map(oldAndNewItems -> oldAndNewItems.mapT1(TodoItem::getState).mapT2(TodoItem::getState))
//                       .flatMap(oldAndNewStates -> {
//                           State oldState = oldAndNewStates.getT1();
//                           State newState = oldAndNewStates.getT2();
//                           if (TODO.equals(oldState) && DOING.equals(newState)) {
//                               return Mono.just(new ItemStartedEvent(updatedTodoItem.getId()));
//                           } else return Mono.empty();
//                       })
        return updatedItemWithOtherValue.doOnNext(iStoreItemState::updateItem)
                .map(ItemUpdatedEvent::new)
                .doOnNext(iPublishStateChange::publish)
                .then(updatedItemWithOtherValue);
    }

    @Override
    public Mono<Void> deleteById(int id) {
        return iStoreItemState.getById(id)
                              .map(todoItem -> new ItemDeletedEvent(id))
                              .flatMap(iPublishStateChange::publish);
    }
}
