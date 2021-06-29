package com.bbby.aem.core.processes.impl;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

final class NameUtil {
    static final String PATH_SEPARATOR = "/";
    static final String VALID_NAME_REGEXP = "(\\w|-)+";
    static final String[] CASE_SENSITIVE_HYPHEN_LABEL_CHAR_MAPPING = new String[] {"-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "-", "-", "-", "-", "-", "-", "-", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "-", "-", "-", "-", "_", "-", "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "-", "-", "-", "-", "-", "-", "f", "-", "-", "-", "fi", "fi", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "y", "-", "-", "-", "-", "i", "c", "p", "o", "v", "-", "s", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "-", "a", "a", "a", "a", "ae", "a", "ae", "c", "e", "e", "e", "e", "i", "i", "i", "i", "d", "n", "o", "o", "o", "o", "oe", "x", "o", "u", "u", "u", "ue", "y", "b", "ss", "a", "a", "a", "a", "ae", "a", "ae", "c", "e", "e", "e", "e", "i", "i", "i", "i", "o", "n", "o", "o", "o", "o", "oe", "-", "o", "u", "u", "u", "ue", "y", "b", "y"};

    private NameUtil() {}

    static String createValidDamName(String title, String[] labelCharMapping, String defaultReplacementCharacter) {
        char[] characters = title.toCharArray();
        StringBuilder name = new StringBuilder(characters.length);

        for(int idx = 0; idx < title.length(); ++idx) {
            char c = title.charAt(idx);
            String replacement = defaultReplacementCharacter;
            if (c < labelCharMapping.length) {
                replacement = labelCharMapping[c];
            }

            name.append(replacement);
        }

        return name.toString();
    }

    static String createValidDamName(String title) {
        return createValidDamName(title, CASE_SENSITIVE_HYPHEN_LABEL_CHAR_MAPPING, "-");
    }

    static String createValidDamPath(String path) {
        if (StringUtils.isNotEmpty(path)) {
            path = Arrays.asList(StringUtils.split(path, PATH_SEPARATOR))
                    .stream()
                    .map(name -> name.matches(VALID_NAME_REGEXP) ? name : NameUtil.createValidDamName(name))
                    .collect(Collectors.joining(PATH_SEPARATOR));
            if (!path.startsWith(PATH_SEPARATOR)) {
                path = PATH_SEPARATOR + path;
            }
        }
        return path;
    }

}