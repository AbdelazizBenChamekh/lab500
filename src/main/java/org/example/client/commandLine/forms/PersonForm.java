package org.example.client.commandLine.forms;

import org.example.client.Exception.ExceptionInFileMode;
import org.example.client.commandLine.*;
import org.example.common.models.*;
import org.example.common.utility.ConsoleColors;
import org.example.client.utility.ExecuteFileManager;

/**
 * Form for creating Person
 */
public class PersonForm extends Form<Person>{

    private final Printable console;
    private final UserInput scanner;

    public PersonForm(Printable console) {
        this.console = (Console.isFileMode())
                ? new BlankConsole()
                : console;
        this.scanner = (Console.isFileMode())
                ? new ExecuteFileManager()
                : new ConsoleInput();
    }

    /**
     * Construct a new element of class {@link Person}
     * @return an object of class {@link Person}
     */
    @Override
    public Person build() {
        console.println(ConsoleColors.toColor("Создание объекта админа", ConsoleColors.PURPLE));
        Person person = new Person(
                askName(),
                askWeight(),
                askEyeColor(),
                askHairColor(),
                askNationality(),
                askLocation()
        );
        console.println(ConsoleColors.toColor("Создание объекта админа окончено успешно", ConsoleColors.PURPLE));
        return person;
    }

    private String askName(){
        String name;
        while (true){
            console.println(ConsoleColors.toColor("Введите имя", ConsoleColors.GREEN));
            name = scanner.nextLine().trim();
            if (name.isEmpty()){
                console.printError("Имя не может быть пустым");
                if (Console.isFileMode()) throw new ExceptionInFileMode();
            }
            else{
                return name;
            }
        }
    }

    private int askWeight(){
        while (true) {
            console.println(ConsoleColors.toColor("Введите количество студентов", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException exception) {
                console.printError("Число студентов должно быть числом типа long");
                if (Console.isFileMode()) throw new ExceptionInFileMode();
            }
        }
    }

    private Color askEyeColor() {
        Color[] values = Color.values();
        while (true) {
            console.println(ConsoleColors.toColor("Возможные цвета глаз:", ConsoleColors.GREEN));
            for (int i = 0; i < values.length; i++) {
                console.println((i + 1) + ") " + values[i]);
            }
            console.println(ConsoleColors.toColor("Введите номер цвета глаз:", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < values.length) {
                    return values[index];
                } else {
                    console.printError("Нет цвета с таким номером.");
                }
            } catch (NumberFormatException e) {
                console.printError("Введите корректный номер.");
            }
            if (Console.isFileMode()) throw new ExceptionInFileMode();
        }
    }

    private Color askHairColor() {
        Color[] values = Color.values();
        while (true) {
            console.println(ConsoleColors.toColor("Возможные цвета волос:", ConsoleColors.GREEN));
            for (int i = 0; i < values.length; i++) {
                console.println((i + 1) + ") " + values[i]);
            }
            console.println(ConsoleColors.toColor("Введите номер цвета волос:", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < values.length) {
                    return values[index];
                } else {
                    console.printError("Нет цвета с таким номером.");
                }
            } catch (NumberFormatException e) {
                console.printError("Введите корректный номер.");
            }
            if (Console.isFileMode()) throw new ExceptionInFileMode();
        }
    }


    private Country askNationality() {
        Country[] values = Country.values();
        while (true) {
            console.println(ConsoleColors.toColor("Возможные страны:", ConsoleColors.GREEN));
            for (int i = 0; i < values.length; i++) {
                console.println((i + 1) + ") " + values[i]);
            }
            console.println(ConsoleColors.toColor("Введите номер страны:", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < values.length) {
                    return values[index];
                } else {
                    console.printError("Нет страны с таким номером.");
                }
            } catch (NumberFormatException e) {
                console.printError("Введите корректный номер.");
            }
            if (Console.isFileMode()) throw new ExceptionInFileMode();
        }
    }
    private Location askLocation(){
        return new LocationForm(console).build();
    }
}
