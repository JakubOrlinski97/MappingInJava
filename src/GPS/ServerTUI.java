package GPS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ServerTUI extends Thread {
    Scanner sc;
    List<String> commands = Arrays.asList("deploy", "add", "remove", "connected", "users", "location");
    Server server;

    public ServerTUI(Server server) {
        this.server = server;
        sc = new Scanner(System.in);
    }

    @Override
    public void run() {
        System.out.println("What would you like to do? " + commands.toString());
        while (sc.hasNext()) {
            String input = sc.nextLine();
            if (commands.contains(input.split(" ")[0])) {
                handleInput(input);
            }
            System.out.println("What would you like to do? " + commands.toString());
        }
    }

    private void handleInput(String input) {
        String[] parts = input.split(" ");

        switch (parts[0]) {
            case "deploy":
                server.getDbHandler().deploy();
                break;
            case "add":
                System.out.println("What should the client's id be?");
                String id = sc.next().split("@")[0];
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
            case "users":
                ArrayList<String> users = server.getDbHandler().getUsers();
                System.out.println("The list of users:\n" + users.toString());
                break;
            case "location":
                System.out.println("What is the id of the user?");
                String client_id = sc.next();
                ArrayList<ArrayList<String>> locations = server.getDbHandler().getLocation(client_id);
                if (locations != null) {
                    for (ArrayList<String> location: locations) {
                        System.out.println("Latitude: " + location.get(0) + "\t\tLongitude: " + location.get(1) + "\t\tTime: " + location.get(2));
                    }
                }
                break;
        }
    }
}
