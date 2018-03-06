package GPS;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Helper;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {
    private static final int memory = 3000;
    private static final int threads = 5;
    private static final long maxMillis = 250;

    private static final int port = 3000;

    private ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private TransactionHandler dbHandler;
    private Argon2 argon2 = Argon2Factory.create();



    public static void main(String[] args) {
        Server server = new Server();
        ServerTUI serverTUI = new ServerTUI(server);

        serverTUI.start();
        server.doStuff();
    }

    private void doStuff() {
        dbHandler = new TransactionHandler();
        System.out.println("Listening on port: " + port);

        try {
            ServerSocket ssock = new ServerSocket(port);

            while (clientHandlers.size() < 1000) {
                Socket client = ssock.accept();
                System.out.println("Connection established with: " + client.getRemoteSocketAddress());
                ClientHandler cl = new ClientHandler(client, dbHandler);
                cl.start();

                clientHandlers.add(cl);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class ClientHandler extends Thread {
        PrintWriter fileWriter;
        BufferedReader in;
        BufferedWriter out;
        Socket client;
        String clientId;
        TransactionHandler dbHandler;

        private ClientHandler (Socket client, TransactionHandler dbHandler) {
            this.dbHandler = dbHandler;
            this.client = client;
            try {
                in =  new BufferedReader(new InputStreamReader(client.getInputStream()));
                out =  new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                System.out.println("Reading from reader...");

                String input = in.readLine();

                while (input != null && !input.equals("end")) {
                    String[] message = input.split(" ");

                    handleInput(message);

                    input = in.readLine();

                }

            } catch (IOException e) {
                System.out.println("OOPS a exception got caught");
            }

            finally {
                System.out.println("Shutting down client: " + "...");
                shutdown();
            }
        }

        private void handleInput(String[] input) {
            System.out.println(Arrays.toString(input));
            try {
                switch (input[0]) {
                    case "login":
                        if (validLogin(input[1], input[2])) {
                            clientId = input[1];
                            out.write("welcome true\n");
                            out.flush();
                        } else {
                            out.write("welcome false\n");
                            shutdown();
                        }
                        break;
                    case "location":
                        if (input[1].equals(clientId)) {
                            dbHandler.addNewLocation(clientId, input[2], input[3], input[4]);
                        }
                        break;
                    case "exit":
                        shutdown();
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        public void shutdown() {
            try {
                fileWriter.close();
                client.shutdownInput();
                client.shutdownOutput();
                client.close();
            } catch (IOException e) {
                System.out.println("Couldn't close connection...");
            }
        }

        private boolean validLogin(String id, String password) {
            return getHash(password).equals(dbHandler.getHash(id));
        }

    }


    public String getHash(String password) {
        System.out.println("Optimal iterations: " + Argon2Helper.findIterations(argon2, maxMillis, memory, threads));
        String hash = argon2.hash(Argon2Helper.findIterations(argon2, maxMillis, memory, threads), memory, threads, password.toCharArray());
        argon2.wipeArray(password.toCharArray());
        return hash;
    }


    public TransactionHandler getDbHandler() {
        return dbHandler;
    }

    public ArrayList<ClientHandler> getClientHandlers() {
        return clientHandlers;
    }
}
