package GPS;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.Socket;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;

public class Server {
    private static final int memory = 3000;
    private static final int threads = 5;
    private static final int iterations = 30;

    private static final int port = 3000;

    private ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private TransactionHandler dbHandler;
    private Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);



    public static void main(String[] args) {
        Server server = new Server();
        ServerTUI serverTUI = new ServerTUI(server);

        serverTUI.start();
        server.doStuff();
    }

    private void doStuff() {
        dbHandler = new TransactionHandler();
        System.out.println("Listening on port: " + port);

        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        System.setProperty("javax.net.ssl.keyStore", "jake.store");
        System.setProperty("javax.net.ssl.keyStorePassword", "c29tZXNhbHQ$sQzx9miPfXJ8JwVzh1urZX4FoLbuOolcJ+HPYl7pH7s");
        //System.setProperty("javax.net.debug","all");

        try {
            SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket ssock = (SSLServerSocket) sslServerSocketfactory.createServerSocket(port);


            //ServerSocket ssock = new ServerSocket(port);

            while (clientHandlers.size() < 1000) {
                SSLSocket client = (SSLSocket) ssock.accept();
                System.out.println("Connection established with: " + client.getRemoteSocketAddress());
                ClientHandler cl = new ClientHandler(client, dbHandler);
                cl.start();

                clientHandlers.add(cl);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeUser(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    private class ClientHandler extends Thread {
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
                            System.out.println("WELCOME " + clientId + input[1]);
                            out.write("welcome true\n");
                            out.flush();
                        } else {
                            System.out.println("NOT WELCOME");
                            out.write("welcome false\n");
                            shutdown();
                        }
                        break;
                    case "location":
                        System.out.println("We good. " + input[1] + " = " + clientId);
                        dbHandler.addNewLocation(input[1], input[2], input[3], input[4]);
                        break;
                    case "exit":
                        shutdown();
                }
            } catch (IOException e) {
                shutdown();
            }
        }

        private void shutdown() {
            try {
                in.close();
                out.close();
                client.close();
                removeUser(this);
            } catch (IOException e) {
                System.out.println("Couldn't close connection...");
            }
        }

        private boolean validLogin(String id, String password) {
            return argon2.verify(dbHandler.getHash(id), password);
        }

    }


    public String getHash(String password) {
        String hash = argon2.hash(iterations, memory, threads, password.toCharArray());
        System.out.println("Hash: " + hash);
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
