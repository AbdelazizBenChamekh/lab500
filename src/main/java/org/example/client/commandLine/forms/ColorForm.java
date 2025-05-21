package org.example.client.commandLine.forms;

import org.example.client.commandLine.*;
import org.example.client.Exception.ExceptionInFileMode;
import org.example.common.models.Color;
import org.example.common.utility.ConsoleColors;
import org.example.client.utility.ExecuteFileManager;

import java.util.Locale;

/**
 * Color selection form
 */
public class ColorForm extends Form<Color>{
    private final Printable console;
    private final UserInput scanner;
    private final String type;

    public ColorForm(Printable console, String type) {
        this.console = (Console.isFileMode())
                ? new BlankConsole()
                : console;
        this.type = type;
        this.scanner = (Console.isFileMode())
                ? new ExecuteFileManager()
                : new ConsoleInput();
    }
    /**
     * Construct a new element of class {@link Color}
     * @return an object of class {@link Color}
     */
    @Override
    public Color build() {
        console.println("Возможные цвета: ");
        console.println(Color.names());
        while (true){
            console.println(ConsoleColors.toColor("Введите цвет " + type + ": ", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try{
                return Color.valueOf(input.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception){
                console.printError("Такого цвета нет в списке");
                if (Console.isFileMode()) throw new ExceptionInFileMode();
            }
        }
    }
}
