package org.example.client.commandLine.forms;

import org.example.client.Exception.ExceptionInFileMode;
import org.example.client.commandLine.*;
import org.example.common.models.Coordinates;
import org.example.common.utility.ConsoleColors;
import org.example.client.utility.ExecuteFileManager;

/**
 * Coordinates form
 */
public class CoordinatesForm extends Form<Coordinates>{
    private final Printable console;
    private final UserInput scanner;

    public CoordinatesForm(Printable console) {
        this.console = (Console.isFileMode())
                ? new BlankConsole()
                : console;
        this.scanner = (Console.isFileMode())
                ? new ExecuteFileManager()
                : new ConsoleInput();
    }

    /**
     * Construct a new element of class {@link Coordinates}
     * @return an object of class {@link Coordinates}
     */
    @Override
    public Coordinates build(){
        return new Coordinates(askX(), askY());
    }

    private Integer askX() {
        while (true) {
            console.println(ConsoleColors.toColor("Введите координату X", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                console.printError("X должно быть числом типа integer");
                if (Console.isFileMode()) throw new ExceptionInFileMode();
            }
        }
    }

    private Integer askY() {
        while (true) {
            console.println(ConsoleColors.toColor("Введите координату Y", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try {
                Integer y = Integer.parseInt(input);
                if (y > 405) {
                    console.printError("Y не должно быть больше 405");
                    if (Console.isFileMode()) throw new ExceptionInFileMode();
                    continue;
                }
                return y;
            } catch (NumberFormatException exception) {
                console.printError("Y должно быть числом типа integer");
                if (Console.isFileMode()) throw new ExceptionInFileMode();
            }
        }
    }

}
