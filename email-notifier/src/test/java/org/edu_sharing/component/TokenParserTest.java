package org.edu_sharing.component;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TokenParserTest {

    @Test
    void variablePresent() throws TokenParserException {
        Map<String, Object> context = Map.of("FooProp", "Foo", "BarProp", "Bar");
        String parsedText = TokenParser.parseExpression("Hello {{FooProp}} World {{ BarProp }}!", context);
        assertEquals("Hello Foo World Bar!", parsedText);
    }
    @Test
    void variableNotSetPresent() throws TokenParserException {
        Map<String, Object> context = new HashMap<>();
        String parsedText = TokenParser.parseExpression("Hello {{FooProp}} World {{ BarProp }}!", context);
        assertEquals("Hello {{FooProp}} World {{ BarProp }}!", parsedText);
    }

    @Test
    void ifReplacement() throws TokenParserException {
        Map<String, Object> context = Map.of("FooProp", "Foo");
        String parsedText = TokenParser.parseExpression("Hello {{ if FooProp }}Foo{{ endIf }}{{ if !BarProp }}World{{ endIf }}!", context);
        assertEquals("Hello Foo!", parsedText);
    }
}