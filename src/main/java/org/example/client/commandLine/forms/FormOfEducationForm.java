package org.example.client.commandLine.forms;

import org.example.client.commandLine.*;
import org.example.client.Exception.ExceptionInFileMode;
import org.example.common.models.FormOfEducation;
import org.example.common.utility.ConsoleColors;
import org.example.client.utility.ExecuteFileManager;

import java.util.Locale;

/**
 * Form for FormOfEducation
 */
public class FormOfEducationForm extends Form<FormOfEducation>{
    private final Printable console;
    private final UserInput scanner;

    public FormOfEducationForm(Printable console) {
        this.console = (Console.isFileMode())
                ? new BlankConsole()
                : console;
        this.scanner = (Console.isFileMode())
                ? new ExecuteFileManager()
                : new ConsoleInput();
    }

    /**
     * Construct a new Enum {@link FormOfEducation}
     * @return an Enum {@link FormOfEducation} object
     */
    @Override
    public FormOfEducation build() {
        console.println("Возможные формы обучения: ");
        console.println(FormOfEducation.names());
        while (true){
            console.println(ConsoleColors.toColor("Введите форму обучения: ", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try{
                return FormOfEducation.valueOf(input.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception){
                console.printError("Такой формы обучения нет в списке");
                if (Console.isFileMode()) throw new ExceptionInFileMode();
            }
        }
    }
}