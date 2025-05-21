package org.example.client.utility;

import org.example.client.commandLine.Console;
import org.example.common.utility.ConsoleColors;
import org.example.client.commandLine.Printable;
import org.example.client.commandLine.forms.StudyGroupForm;
import org.example.common.network.Request;
import org.example.common.network.Response;
import org.example.common.network.StatusCode;
import org.example.client.Exception.ExceptionInFileMode;
import org.example.client.Exception.ExitObliged;
import org.example.client.Exception.InvalidForm;
import org.example.common.models.StudyGroup;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Scanner;

import static org.example.common.network.StatusCode.*;

/**
 * User Input Handling Class
 */
public class RuntimeManager {
    private final Printable console;
    private final Scanner userScanner;
    private final Client client;

    public RuntimeManager(Printable console, Scanner userScanner, Client client) {
        this.console = console;
        this.userScanner = userScanner;
        this.client = client;
    }

    /**
     * Permanent work with the user and execution of commands
     */
    public void interactiveMode(){
        while (true) {
            try {
                if (!userScanner.hasNext()) throw new ExitObliged();
                String[] userCommand = (userScanner.nextLine().trim() + " ").split(" ", 2);
                Response response = client.sendAndAskResponse(new Request(userCommand[0].trim(), userCommand[1].trim()));
                this.printResponse(response);

                switch (response.getStatus()){
                    case ASK_OBJECT -> {
                        StudyGroup studyGroup = new StudyGroupForm(console).build();
                        if(!studyGroup.validate()) throw new InvalidForm();
                        Response newResponse = client.sendAndAskResponse(
                                new Request(
                                        userCommand[0].trim(),
                                        userCommand[1].trim(),
                                        studyGroup));
                        if (newResponse.getStatus() != OK){
                            console.printError(newResponse.getResponse());
                        }
                        else {
                            this.printResponse(newResponse);
                        }
                    }
                    case EXIT -> throw new ExitObliged();
                    case EXECUTE_SCRIPT -> {
                        Console.setFileMode(true);
                        this.fileExecution(response.getResponse());
                        Console.setFileMode(false);
                    }
                    default -> {}
                }
            } catch (InvalidForm err){
                console.printError("Поля не валидны! Объект не создан");
            } catch (NoSuchElementException exception) {
                console.printError("Пользовательский ввод не обнаружен!");
            } catch (ExitObliged exitObliged){
                console.println(ConsoleColors.toColor("До свидания!", ConsoleColors.YELLOW));
                return;
            } catch (IOException | ClassNotFoundException e) {
                console.printError("Ошибка связи с сервером: " + e.getMessage());
                // Optionally, add retry logic or break here
            }
        }
    }

    private void printResponse(Response response){
        switch (response.getStatus()){
            case OK -> {
                if (Objects.isNull(response.getCollection())) {
                    console.println(response.getResponse());
                } else {
                    console.println(response.getResponse() + "\n" + response.getCollection().toString());
                }
            }
            case ERROR -> console.printError(response.getResponse());
            case WRONG_ARGUMENTS -> console.printError("Неверное использование команды!");
            default -> {}
        }
    }

    private void fileExecution(String args) throws ExitObliged{
        if (args == null || args.isEmpty()) {
            console.printError("Путь не распознан");
            return;
        }
        else console.println(ConsoleColors.toColor("Путь получен успешно", ConsoleColors.PURPLE));
        args = args.trim();
        try {
            ExecuteFileManager.pushFile(args);
            for (String line = ExecuteFileManager.readLine(); line != null; line = ExecuteFileManager.readLine()) {
                String[] userCommand = (line + " ").split(" ", 2);
                userCommand[1] = userCommand[1].trim();
                if (userCommand[0].isBlank()) return;
                if (userCommand[0].equals("execute_script")){
                    if(ExecuteFileManager.fileRepeat(userCommand[1])){
                        console.printError("Найдена рекурсия по пути " + new File(userCommand[1]).getAbsolutePath());
                        continue;
                    }
                }
                console.println(ConsoleColors.toColor("Выполнение команды " + userCommand[0], ConsoleColors.YELLOW));
                Response response = client.sendAndAskResponse(new Request(userCommand[0].trim(), userCommand[1].trim()));
                this.printResponse(response);
                switch (response.getStatus()){
                    case ASK_OBJECT -> {
                        StudyGroup studyGroup;
                        try{
                            studyGroup = new StudyGroupForm(console).build();
                            if (!studyGroup.validate()) throw new ExceptionInFileMode();
                        } catch (ExceptionInFileMode err){
                            console.printError("Поля в файле не валидны! Объект не создан");
                            continue;
                        }
                        Response newResponse = client.sendAndAskResponse(
                                new Request(
                                        userCommand[0].trim(),
                                        userCommand[1].trim(),
                                        studyGroup));
                        if (newResponse.getStatus() != StatusCode.OK){
                            console.printError(newResponse.getResponse());
                        }
                        else {
                            this.printResponse(newResponse);
                        }
                    }
                    case EXIT -> throw new ExitObliged();
                    case EXECUTE_SCRIPT -> {
                        this.fileExecution(response.getResponse());
                        ExecuteFileManager.popRecursion();
                    }
                    default -> {}
                }
            }
            ExecuteFileManager.popFile();
        } catch (FileNotFoundException fileNotFoundException){
            console.printError("Такого файла не существует");
        } catch (IOException | ClassNotFoundException e) {
            console.printError("Ошибка связи с сервером: " + e.getMessage());
        }
    }
}
