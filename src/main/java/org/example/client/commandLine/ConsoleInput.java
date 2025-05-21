package org.example.client.commandLine;

import org.example.client.utility.ScannerManager;

import java.util.Scanner;

/**
 * Class for standard input via console
 */
public class ConsoleInput implements UserInput{
    private static final Scanner userScanner = ScannerManager.getUserScanner();

    @Override
    public String nextLine() {
        return userScanner.nextLine();
    }
}
