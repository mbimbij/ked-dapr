package com.example.javaapp.utils;

import org.assertj.core.matcher.AssertionMatcher;
import org.mockito.hamcrest.MockitoHamcrest;

import java.util.function.Consumer;

public class TestUtil {
    public static <T> T argThat(Consumer<T> assertions) {
        return MockitoHamcrest.argThat(new AssertionMatcher<T>() {
            @Override
            public void assertion(T actual) throws AssertionError {
                assertions.accept(actual);
            }
        });
    }
}
