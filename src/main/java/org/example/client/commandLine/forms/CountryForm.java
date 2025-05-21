package org.example.client.commandLine.forms;

import org.example.client.commandLine.*;
import org.example.client.Exception.ExceptionInFileMode;
import org.example.common.models.Country;
import org.example.common.utility.ConsoleColors;
import org.example.client.utility.ExecuteFileManager;

import java.util.Locale;

/**
 * A form for countries
 */
public class CountryForm extends Form<Country>{
    private final Printable console;
    private final UserInput scanner;

    public CountryForm(Printable console) {
        this.console = (Console.isFileMode())
                ? new BlankConsole()
                : console;
        this.scanner = (Console.isFileMode())
                ? new ExecuteFileManager()
                : new ConsoleInput();
    }

    /**
     * Constructs new element Enum {@link CountryForm}
     * @return object Enum {@link CountryForm}
     */
    @Override
    public Country build() {
        console.println("Возможные страны: ");
        console.println(Country.names());
        while (true){
            console.println(ConsoleColors.toColor("Введите страну: ", ConsoleColors.GREEN));
            String input = scanner.nextLine().trim();
            try{
                return Country.valueOf(input.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception){
                console.printError("Такой страны нет в списке");
                if (Console.isFileMode()) throw new ExceptionInFileMode();
            }
        }
    }
}
