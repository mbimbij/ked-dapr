package com.example.javaapp.core;

import com.example.javaapp.core.events.ItemCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.List;
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
    public List<TodoItem> getAll() {
        return null;
    }

    @Override
    public TodoItem updateItem(TodoItem updatedTodoItem) {
        return null;
    }

    @Override
    public void deleteById(int id) {

    }
}
