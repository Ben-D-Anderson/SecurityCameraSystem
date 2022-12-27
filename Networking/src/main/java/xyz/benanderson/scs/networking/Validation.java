package xyz.benanderson.scs.networking;

public class Validation {

    public static int parsePort(String portInputStr) throws ValidationException {
        try {
            int port = Integer.parseInt(portInputStr);
            return parsePort(port);
        } catch (NumberFormatException numberFormatException) {
            throw new ValidationException("Port number must be an integer");
        }
    }

    public static int parsePort(int portInput) throws ValidationException {
        if (portInput < 0 || portInput > 65535) {
            throw new ValidationException("Port number must be between 1 and 65535 (inclusive)");
        }
        return portInput;
    }

    public static class ValidationException extends Exception {
        public ValidationException(String s) {
            super(s);
        }
    }

}
