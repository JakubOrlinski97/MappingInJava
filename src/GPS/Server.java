package GPS;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private static final int port = 3000;
    private ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    public static void main(String[] args) {
        Server server = new Server();
        server.doStuff();
    }

    private void doStuff() {
        System.out.println("Listening on port: " + port);

        try {
            ServerSocket ssock = new ServerSocket(port);

            while (clientHandlers.size() < 1000) {
                Socket client = ssock.accept();
                System.out.println("Connection established with: " + client.getRemoteSocketAddress());
                ClientHandler cl = new ClientHandler(client);
                cl.start();

                clientHandlers.add(cl);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class ClientHandler extends Thread {
        Socket client;

        private ClientHandler (Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            PrintWriter fileWriter;
            BufferedReader reader;
            try {
                System.out.println("Reading from reader...");


                reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

                String input = reader.readLine();

                while (input != null && !input.equals("end")) {
                    String[] location = input.split(" ");
                    System.out.println("Latitude: " + location[0] + "\nLongitude: " + location[1]);

                    fileWriter = new PrintWriter(new FileWriter(new File("logs/" + client.getRemoteSocketAddress() + ".log"), true));

                    fileWriter.println(location[0] + " " + location[1]);

                    fileWriter.close();
                    input = reader.readLine();

                }

            } catch (IOException e) {
                System.out.println("OOPS a exception got caught");
            }

            finally {
                try {
                    System.out.println("Shutting down client: " + "...");
                    client.shutdownOutput();
                    client.shutdownInput();
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
