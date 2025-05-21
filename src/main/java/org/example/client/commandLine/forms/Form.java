package org.example.client.commandLine.forms;

import org.example.client.Exception.InvalidForm;

/**
 * Abstract class for custom input forms
 * @param <T> form class
 */
public abstract class Form<T>{
    public abstract T build() throws InvalidForm;
}