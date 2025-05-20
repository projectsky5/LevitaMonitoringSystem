package com.levita.levita_monitoring.service.parser;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameAndLocationParserImplTest {

    private final NameAndLocationParserImpl parser = new NameAndLocationParserImpl();

    @Test
    void parse_nullRaw_returnsEmptyNameAndLocation() {
        String[] result = parser.parse(null);
        assertArrayEquals(new String[]{ "", "" }, result);
    }

    @Test
    void parse_emptyString_returnsEmptyNameAndLocation() {
        String[] result = parser.parse("");
        assertArrayEquals(new String[]{ "", "" }, result);
    }

    @Test
    void parse_plainNameWithoutParentheses_returnsNameAndEmptyLocation() {
        String[] result = parser.parse("Bob");
        assertArrayEquals(new String[]{ "Bob", "" }, result);
    }

    @Test
    void parse_nameAndLocationWithSpace_returnsTrimmedParts() {
        String[] result = parser.parse("Alice (Paris)");
        assertArrayEquals(new String[]{ "Alice", "Paris" }, result);
    }

    @Test
    void parse_nameAndLocationWithoutSpace_returnsTrimmedParts() {
        String[] result = parser.parse("Charlie( Berlin )");
        assertArrayEquals(new String[]{ "Charlie", "Berlin" }, result);
    }

    @Test
    void parse_multipleParentheses_returnsWholeAndEmpty() {
        String raw = "A (B) extra";
        String[] result = parser.parse(raw);
        assertArrayEquals(new String[]{ raw.trim(), "" }, result);
    }

    @Test
    void parse_onlyParentheses_returnsCleanedAndEmptyLocation() {
        assertArrayEquals(new String[]{ "()", "" }, parser.parse("()"));
    }
}