package org.edu_sharing.component;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.commons.lang.StringUtils;

public class TokenParser {

    public static final String START_TOKEN = "{{";
    public static final String END_TOKEN = "}}";
    final String text;
    final Map<String, Object> context;


    public TokenParser(String text, Map<String, Object> context) {
        this.text = text;
        this.context = context;
    }

    public static String parseExpression(String text, Map<String, Object> context) throws TokenParserException {
        return new TokenParser(text, context).parseExpression();
    }

    private String parseExpression() throws TokenParserException {
        StringBuilder result = new StringBuilder(text.length());
        int currentPos = 0;
        while (currentPos < text.length()) {
            int startTokenPos = text.indexOf(START_TOKEN, currentPos);
            if (startTokenPos == -1) {
                result.append(text.substring(currentPos));
                break;
            }
            result.append(text, currentPos, startTokenPos);

            int endTokenPos = text.indexOf(END_TOKEN, startTokenPos);
            if (endTokenPos == -1) {
                throw new TokenParserException("'}}' expected");
            }

            String token = text.substring(startTokenPos, endTokenPos + END_TOKEN.length());
            String tokenContent = text.substring(startTokenPos + START_TOKEN.length(), endTokenPos).trim();


            Object property = parseProperty(startTokenPos, tokenContent);
            if (property != null && !(property instanceof List<?>) && !(property instanceof Map<?, ?>)) {
                result.append(property);
            } else {
                result.append(token);
            }

            currentPos = endTokenPos + END_TOKEN.length();
        }
        return result.toString();
    }

    private Object tokenParser(int startTokenPos, int endTokenPosition, String tokenContent) throws TokenParserException {

        String delim = " ";
        StringTokenizer tokenizer = new StringTokenizer(tokenContent, delim);

        int i = startTokenPos;
        String token = tokenizer.nextToken();
        i += token.length();
        if (token.equals("if")) {
//            boolean negate=false;
//            while (tokenizer.hasMoreTokens()) {
//                token = tokenizer.nextToken();
//                if(token.equals("!")){
//                    negate = !negate;
//                } else {
//                    Object property = parseProperty(i, token);
//                    if(property != null && StringUtils.isNotBlank(property.toString())){
//                        return negate;
//                    }
//                    return !negate;
//                }
//            }
        } else {
            return parseProperty(startTokenPos, tokenContent);
        }


        return null;
    }

    private Object parseProperty(int startPos, String tokenContent) throws TokenParserException {
        Object data = context;

        String delim = ".[]";
        StringTokenizer tokenizer = new StringTokenizer(tokenContent, delim);
        int i = startPos;
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            i += token.length();

            if (data == null) {
                return null;
            }

            if (data instanceof Map<?, ?> map) {
                data = map.get(token);
            } else if (data instanceof List<?> list) {
                try {
                    data = list.get(Integer.parseInt(token));

                } catch (NumberFormatException ex) {
                    throw new TokenParserException("Unexpected token '" + token + "' at \"" + text.substring(startPos, i) + "\" at pos: " + i);
                }
            } else {
                if (tokenizer.hasMoreTokens()) {
                    return null;
                }
                return data;
            }
        }

        if (tokenizer.hasMoreTokens()) {
            return null;
        }

        return data;
    }
}

