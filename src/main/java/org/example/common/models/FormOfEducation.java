package org.example.common.models;

import java.io.Serializable;

/**
 * Enum representing forms of education.
 */
public enum FormOfEducation implements Serializable {
    DISTANCE_EDUCATION,
    FULL_TIME_EDUCATION,
    EVENING_CLASSES;

    /**
     * @return перечисляет по строкам все элементы Enum
     */
    public static String names() {
        StringBuilder nameList = new StringBuilder();
        for (var forms : values()) {
            nameList.append(forms.name()).append("\n");
        }
        return nameList.substring(0, nameList.length()-1);
    }

}