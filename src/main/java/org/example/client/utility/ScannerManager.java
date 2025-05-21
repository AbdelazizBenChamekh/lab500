package org.example.client.utility;

import java.util.Scanner;

/**
 * Class storing scanner for program
 */
public class ScannerManager {
    public static Scanner userScanner = new Scanner(System.in);

    public static Scanner getUserScanner() {
        return userScanner;
    }
}