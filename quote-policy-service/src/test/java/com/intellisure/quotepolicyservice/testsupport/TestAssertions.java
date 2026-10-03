package com.intellisure.quotepolicyservice.testsupport;

import org.hamcrest.Matcher;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class TestAssertions {

    private TestAssertions() {
    }

    public static void errorIs(
            Class<? extends Throwable> type,
            String expectedMessage,
            Throwable actual
    ) {
        assertInstanceOf(type, actual);
        assertEquals(expectedMessage, actual.getMessage());
    }

    public static void errorIs(
            Class<? extends Throwable> type,
            Matcher<? super String> expectedMessage,
            Throwable actual
    ) {
        assertInstanceOf(type, actual);
        MatcherAssertHelper.assertThat(actual.getMessage(), expectedMessage);
    }

    public static void messageIs(
            String expected,
            Throwable actual
    ) {
        assertEquals(expected, actual.getMessage());
    }

    public static void messageIs(
            Matcher<? super String> expected,
            Throwable actual
    ) {
        MatcherAssertHelper.assertThat(actual.getMessage(), expected);
    }

    public static void messageContains(
            String expectedFragment,
            Throwable actual
    ) {
        assertTrue(
                actual.getMessage() != null
                        && actual.getMessage().contains(
                                expectedFragment
                        ),
                "Expected message to contain <" + expectedFragment
                        + "> but was <" + actual.getMessage() + ">"
        );
    }

    public static void messageContainsAll(
            List<String> fragments,
            Throwable actual
    ) {
        for (String fragment : fragments) {
            messageContains(fragment, actual);
        }
    }

    private static final class MatcherAssertHelper {

        private MatcherAssertHelper() {
        }

        static void assertThat(
                String actual,
                Matcher<? super String> matcher
        ) {
            org.hamcrest.MatcherAssert.assertThat(actual, matcher);
        }
    }
}
