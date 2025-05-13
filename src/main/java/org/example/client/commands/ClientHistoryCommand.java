package org.example.client.commands;
import org.example.common.network.Request;
import org.example.client.ConsoleReader;
import java.util.List;

public class ClientHistoryCommand implements ClientCommand {
    private final List<String> history;
    public ClientHistoryCommand(List<String> history) { this.history = history; }

    @Override
    public Request prepareRequest(String argumentString, ConsoleReader console) {
        // History is purely client-side, no request needed
        executeLocally(console);
        return null;
    }

    // Helper method for ExecuteScriptCommand to call directly
    public void executeLocally(ConsoleReader console) {
        console.println("--- Client Command History (last " + history.size() + ") ---");
        if (history.isEmpty()) {
            console.println("(No commands executed yet in this session)");
        } else {
            int count = 1;
            for (String cmd : history) {
                console.println(String.format("%2d. %s", count++, cmd));
            }
        }
        console.println("------------------------------------");
    }

    @Override public String getName() { return "history"; }
    @Override public String getDescription() { return "history : show last 12 commands executed by the client"; }
}