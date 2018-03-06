package GPS;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ServerTUI extends Thread {
    Scanner sc;
    List<String> commands = Arrays.asList("deploy", "add", "remove", "connected");
    Server server;

    public ServerTUI(Server server) {
        this.server = server;
        sc = new Scanner(System.in);
    }

    @Override
    public void run() {
        String  input = sc.nextLine();
        while (input != null) {

            if (commands.contains(input.split(" ")[0])) {
                handleInput(input);
            }

            input = sc.nextLine();
        }
    }

    private void handleInput(String input) {
        String[] parts = input.split(" ");

        switch (parts[0]) {
            case "deploy":
                System.out.println("What should the database be called?");
                String filename = sc.next();
                server.getDbHandler().deploy(filename);
                break;
            case "add":
                System.out.println("What should the client's id be?");
                String id = sc.next();
                System.out.println("And what should the password be?");
                String password = sc.next();
                server.getDbHandler().addNewID(id, server.getHash(password));
                break;
            case "remove":
                System.out.println("What id should be removed?");
                String clientId = sc.next();
                server.getDbHandler().removeID(clientId);
                break;
            case "connected":
                System.out.println("Connected users:\n" + server.getClientHandlers().toString());
                break;
        }


    }

}
