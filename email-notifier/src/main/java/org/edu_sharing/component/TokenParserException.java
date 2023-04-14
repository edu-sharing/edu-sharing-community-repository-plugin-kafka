package org.edu_sharing.component;

import java.io.IOException;

public class TokenParserException extends Exception {
    public TokenParserException(String message) {
        super(message);
    }
    public TokenParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenParserException(Throwable cause) {
        super(cause);
    }
}
