package org.example.common.models;

import java.io.Serializable;

/**
 * Enum representing possible eye or hair colors.
 */
public enum Color implements Serializable {
    //private static final long serialVersionUID = 1L;
    GREEN,
    RED,
    BLACK,
    ORANGE,
    BROWN;

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