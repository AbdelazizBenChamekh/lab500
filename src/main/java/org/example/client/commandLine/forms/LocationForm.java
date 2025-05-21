package org.example.client.commandLine.forms;

import org.example.client.Exception.ExceptionInFileMode;
import org.example.client.commandLine.*;
import org.example.common.models.Location;
import org.example.common.utility.ConsoleColors;
import org.example.client.utility.ExecuteFileManager;

/**
 * Form for Location
 */
public class LocationForm extends Form<Location>{
    private final Printable console;
    private final UserInput scanner;

    public LocationForm(Printable console) {
        this.console = (Console.isFileMode())
                ? new BlankConsole()
                : console;
        this.scanner = (Console.isFileMode())
                ? new ExecuteFileManager()
                : new ConsoleInput();
    }

    /**
     * Construct a new element of class {@link Location}
     * @return an object of class {@link Location}
     */
    @Override
    public Location build(){
        return new Location(
                askX(),
                askY(),
                askName());
    }

    private Integer askX() {
        while (true) {
            console.println(ConsoleColors.toColor("Введите координату X (целое число)", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                console.printError("X должно быть числом типа Integer");
                if (Console.isFileMode()) throw new ExceptionInFileMode();
            }
        }
    }

    private double askY() {
        while (true) {
            console.println(ConsoleColors.toColor("Введите координату Y (число с плавающей точкой)", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException exception) {
                console.printError("Y должно быть числом типа double");
                if (Console.isFileMode()) throw new ExceptionInFileMode();
            }
        }
    }

    private String askName() {
        while (true) {
            console.println(ConsoleColors.toColor("Введите название локации (может быть пустым)", ConsoleColors.GREEN));
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                // Since Location allows null name, interpret empty string as null
                return null;
            } else {
                return name;
            }
        }
    }

}
