package org.example.server.network;

import org.example.common.utility.ConsoleColors;

/**
 * Interface combining output methods
 */
public interface Printable {
    void println(String a);
    void print(String a);
    default void println(String a, ConsoleColors consoleColors){
        println(a);
    };
    default void print(String a, ConsoleColors consoleColors){
        print(a);
    };
    void printError(String a);
}
