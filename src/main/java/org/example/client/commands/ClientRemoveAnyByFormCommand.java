package org.example.client.commands;

import org.example.common.models.FormOfEducation; // Need the enum
import org.example.common.network.Request;
import org.example.client.ConsoleReader;

/**
 * Client command for remove_any_by_form_of_education. Parses enum argument.
 */
public class ClientRemoveAnyByFormCommand implements ClientCommand {
    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        if (argumentString == null) {
            console.printError("FormOfEducation argument required.");
            // Maybe print options like ConsoleReader.readEnum does?
            return null;
        }
        try {
            // Validate and convert string to enum locally before sending
            FormOfEducation form = FormOfEducation.valueOf(argumentString.trim().toUpperCase());
            // Send command name and the enum object itself (or its name string)
            // Let's send the String name, server RequestHandler expects string arg here
            return new Request(getName(), form.name());
            // Alt: return new Request(getName(), form); // If Request supported Enum arg directly

        } catch (IllegalArgumentException e) {
            console.printError("Invalid FormOfEducation value: '" + argumentString + "'.");
            // Print valid options
            console.print("Valid options: ");
            for(FormOfEducation f : FormOfEducation.values()) console.print(f.name()+" ");
            console.println("");
            return null;
        }
    }
    @Override public String getName() { return "remove_any_by_form_of_education"; }
    @Override public String getDescription() { return "remove_any_by_form_of_education <form> : remove one element by form"; }
}