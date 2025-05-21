package org.example.common.models;

import java.io.Serializable;

/**
 * Enum representing countries for nationality.
 */
public enum Country implements Serializable {
    USA,
    INDIA,
    THAILAND,
    SOUTH_KOREA,
    JAPAN;

    /**
     * @return перечисляет в строке все элементы Enum
     */
    public static String names() {
        StringBuilder nameList = new StringBuilder();
        for (var forms : values()) {
            nameList.append(forms.name()).append("\n");
        }
        return nameList.substring(0, nameList.length()-1);
    }
}