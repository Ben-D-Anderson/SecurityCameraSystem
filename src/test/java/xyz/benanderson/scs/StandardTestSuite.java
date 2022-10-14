package xyz.benanderson.scs;

import java.util.function.Consumer;

public class StandardTestSuite {

    private static final int[] integers = {
            -1, 0, 1, 2147483647, -2147483647, 1000000, -1000000, -2147483648
    };
    private static final String[] strings = {
            "abc", "a b c", "1234567890", "¡™£¢∞§¶•ªº–≠", "(╯°□°）╯︵ ┻━┻)",
            "❤", ",./;'[]\\-=\n", "<>?:\"{}|_+", "!@#$%^&*()`~", "${{<%[%'\"}}%\\.", "\u200F\u200E\u200E",
            "abcdefghijklmnopqrstuvwxyz", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                                            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                                            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" +
                                            "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    };
    private static final double[] reals = {
            1.23, 1.0, 9999999999999999.0, 0.9999999999999999, 0.0, 1, 0.0000000000000001, 123456789.123456789
    };
    private static final char[] characters = {
            'c', '1', '\u200E', '¶', '❤', '\n'
    };
    private static final boolean[] booleans = {
            true, false
    };

    public static void testInteger(Consumer<Integer> integerConsumer) {
        for (int testInput : integers) {
            integerConsumer.accept(testInput);
        }
    }

    public static void testString(Consumer<String> stringConsumer) {
        for (String testInput : strings) {
            stringConsumer.accept(testInput);
        }
    }

    public static void testReal(Consumer<Double> doubleConsumer) {
        for (double testInput : reals) {
            doubleConsumer.accept(testInput);
        }
    }

    public static void testCharacter(Consumer<Character> characterConsumer) {
        for (char testInput : characters) {
            characterConsumer.accept(testInput);
        }
    }

    public static void testBoolean(Consumer<Boolean> booleanConsumer) {
        for (boolean testInput : booleans) {
            booleanConsumer.accept(testInput);
        }
    }

}
