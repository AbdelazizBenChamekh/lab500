package org.example.client.commandLine.forms;

import org.example.client.Exception.ExceptionInFileMode;
import org.example.client.commandLine.*;
import org.example.common.models.*;
import org.example.common.utility.ConsoleColors;
import org.example.client.utility.ExecuteFileManager;

/**
 * Form for creating a StudyGroup object.
 */
public class StudyGroupForm extends Form<StudyGroup> {
    private final Printable console;
    private final UserInput scanner;

    public StudyGroupForm(Printable console) {
        this.console = (Console.isFileMode())
                ? new BlankConsole()
                : console;
        this.scanner = (Console.isFileMode())
                ? new ExecuteFileManager()
                : new ConsoleInput();
    }

    /**
     * Construct a new StudyGroup object.
     * @return a StudyGroup instance
     */
    @Override
    public StudyGroup build() {
        return new StudyGroup(
                askName(),
                askCoordinates(),
                askStudentsCount(),
                askExpelledStudents(),
                askFormOfEducation(),  // only once here
                askSemesterEnum(),
                askGroupAdmin()        // admin inputs handled inside PersonForm
        );
    }


    private String askName() {
        while (true) {
            console.println(ConsoleColors.toColor("Enter the group name:", ConsoleColors.GREEN));
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                console.printError("Name cannot be empty.");
                if (Console.isFileMode()) throw new ExceptionInFileMode();
            } else {
                return name;
            }
        }
    }

    private Coordinates askCoordinates() {
        return new CoordinatesForm(console).build();
    }

    private long askStudentsCount() {
        while (true) {
            console.println(ConsoleColors.toColor("Enter the number of students (must be > 0):", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try {
                long value = Long.parseLong(input);
                if (value <= 0) {
                    console.printError("The number of students must be greater than 0.");
                    if (Console.isFileMode()) throw new ExceptionInFileMode();
                    continue;
                }
                return value;
            } catch (NumberFormatException exception) {
                console.printError("The number of students must be a long integer.");
                if (Console.isFileMode()) throw new ExceptionInFileMode();
            }
        }
    }

    private Long askExpelledStudents() {
        while (true) {
            console.println(ConsoleColors.toColor("Enter the number of expelled students (must be > 0, or leave empty for none):", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return null;
            try {
                long value = Long.parseLong(input);
                if (value <= 0) {
                    console.printError("The number of expelled students must be greater than 0.");
                    if (Console.isFileMode()) throw new ExceptionInFileMode();
                    continue;
                }
                return value;
            } catch (NumberFormatException exception) {
                console.printError("The number of expelled students must be a long integer.");
                if (Console.isFileMode()) throw new ExceptionInFileMode();
            }
        }
    }

    private FormOfEducation askFormOfEducation() {
        FormOfEducation[] values = FormOfEducation.values();
        while (true) {
            console.println(ConsoleColors.toColor("Возможные формы обучения:", ConsoleColors.GREEN));
            for (int i = 0; i < values.length; i++) {
                console.println((i + 1) + ") " + values[i]);
            }
            console.println(ConsoleColors.toColor("Введите номер формы обучения:", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < values.length) {
                    return values[index];
                } else {
                    console.printError("Нет формы обучения с таким номером.");
                }
            } catch (NumberFormatException e) {
                console.printError("Введите корректный номер.");
            }
            if (Console.isFileMode()) throw new ExceptionInFileMode();
        }
    }


    private Semester askSemesterEnum() {
        Semester[] values = Semester.values();
        while (true) {
            console.println(ConsoleColors.toColor("Возможные семестры:", ConsoleColors.GREEN));
            for (int i = 0; i < values.length; i++) {
                console.println((i + 1) + ") " + values[i]);
            }
            console.println(ConsoleColors.toColor("Введите номер семестра (или оставьте пустым для отсутствия):", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return null;
            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < values.length) {
                    return values[index];
                } else {
                    console.printError("Нет семестра с таким номером.");
                }
            } catch (NumberFormatException e) {
                console.printError("Введите корректный номер.");
            }
            if (Console.isFileMode()) throw new ExceptionInFileMode();
        }
    }
    private Color askColor() {
        Color[] values = Color.values();
        while (true) {
            console.println(ConsoleColors.toColor("Возможные цвета:", ConsoleColors.GREEN));
            for (int i = 0; i < values.length; i++) {
                console.println((i + 1) + ") " + values[i]);
            }
            console.println(ConsoleColors.toColor("Введите номер цвета:", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try {
                int index = Integer.parseInt(input) - 1;  // Convert 1-based input to 0-based index
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



    private Country askCountry() {
        Country[] values = Country.values();
        while (true) {
            console.println(ConsoleColors.toColor("Возможные страны:", ConsoleColors.GREEN));
            for (int i = 0; i < values.length; i++) {
                console.println((i + 1) + ") " + values[i]);
            }
            console.println(ConsoleColors.toColor("Введите номер страны:", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try {
                int index = Integer.parseInt(input) - 1;  // Convert 1-based input to 0-based index
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





    private Person askGroupAdmin() {
        return new PersonForm(console).build();
    }
}
