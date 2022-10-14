package xyz.benanderson.scs;

import java.util.function.Consumer;

public class StandardInputTestSuite {

    private static final String[] integers = {
            "-1", "0", "1", "2147483647", "-2147483647", "1000000", "-1000000",
            "4294967295", "2147483648", "-2147483648", "4294967296", "999999999999999999"
    };
    private static final String[] strings = {
            "abc", "a b c", "1234567890", "¡™£¢∞§¶•ªº–≠", "(╯°□°）╯︵ ┻━┻)",
            "❤", ",./;'[]\\-=\n", "<>?:\"{}|_+", "!@#$%^&*()`~", "${{<%[%'\"}}%\\.", "\u200F\u200E\u200E",
            "abcdefghijklmnopqrstuvwxyz", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                                            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                                            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                                            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    };
    private static final String[] reals = {
            "1.23", "1.0", "9999999999999999.0", "0.9999999999999999", "0.0", "1", "0.0000000000000001", "123456789.123456789"
    };
    private static final String[] characters = {
            "c", "1", "\u200E", "¶", "❤", "\n"
    };
    private static final String[] booleans = {
            "True", "TRUE", "true", "False", "FALSE", "false", "1", "0"
    };
    private static final String[] erroneous = {
            "abc", "a", "123", "1.23", "True", "False", "", null
    };

    public static void testInteger(Consumer<String> integerConsumer) {
        for (String testInput : integers) {
            integerConsumer.accept(testInput);
        }
    }

    public static void testString(Consumer<String> stringConsumer) {
        for (String testInput : strings) {
            stringConsumer.accept(testInput);
        }
    }

    public static void testReal(Consumer<String> doubleConsumer) {
        for (String testInput : reals) {
            doubleConsumer.accept(testInput);
        }
    }

    public static void testCharacter(Consumer<String> characterConsumer) {
        for (String testInput : characters) {
            characterConsumer.accept(testInput);
        }
    }

    public static void testBoolean(Consumer<String> booleanConsumer) {
        for (String testInput : booleans) {
            booleanConsumer.accept(testInput);
        }
    }

    public static void testErroneous(Consumer<String> stringConsumer) {
        for (String erroneousInput : erroneous) {
            stringConsumer.accept(erroneousInput);
        }
    }

}
