package org.example.client;

import org.example.client.commandLine.BlankConsole;
import org.example.client.commandLine.Console;
import org.example.client.commandLine.Printable;
import org.example.client.Exception.IllegalArguments;
import org.example.client.utility.Client;
import org.example.client.utility.RuntimeManager;
import org.example.common.models.FormOfEducation;
import org.example.common.models.Semester;

import java.util.Scanner;
/**
 * Main client App
 */
public class App {
    private static String host;
    private static int port;
    private static Printable console = new BlankConsole();

    public static boolean parseHostPort(String[] args){
        try{
            if(args.length != 2) throw new IllegalArguments("Передайте хост и порт в аргументы " +
                    "командной строки в формате <host> <port>");
            host = args[0];
            port = Integer.parseInt(args[1]);
            if(port < 0) throw new IllegalArguments("Порт должен быть натуральным числом");
            return true;
        } catch (IllegalArguments e) {
            console.printError(e.getMessage());
        }
        return false;
    }

    public static void main(String[] args) {
        Semester semester = Semester.FIRST;
        int ordinal = semester.ordinal() + 1;
        FormOfEducation form = FormOfEducation.FULL_TIME_EDUCATION;
        int oneBased = form.ordinal() + 1;

        if (!parseHostPort(args)) return;
        console = new Console();
        Client client = new Client(host, port, 5000, 5, console);
        new RuntimeManager(console, new Scanner(System.in), client).interactiveMode();
    }
}
