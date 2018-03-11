package GPS;

import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.security.Security;

public class TestClient {
    SSLSocketFactory sslSocketfactory;
    Socket sock;
    int port = 3000;
    String ip = "localhost";
    BufferedReader in;
    BufferedWriter out;


    public static void main(String[] args) {
        SSLSocketFactory sslSocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        System.setProperty("javax.net.ssl.trustStore", "jakeTrust.store");
        System.setProperty("javax.net.ssl.trustStorePassword", "c29tZXNhbHQ$sQzx9miPfXJ8JwVzh1urZX4FoLbuOolcJ+HPYl7pH7s");
        System.setProperty("javax.net.debug","all");

        TestClient client = new TestClient(sslSocketfactory);
    }

    public TestClient(SSLSocketFactory sslSocketfactory) {
        this.sslSocketfactory = sslSocketfactory;

        connect();
    }

    private void connect() {
        try {
            sock = sslSocketfactory.createSocket(ip, port);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream()));

            out.write("login jake pooler");

            String input = in.readLine();

            while (input != null && !input.equals("end")) {
                String[] message = input.split(" ");

                if (message[0].equals("welcome")) {
                    if (message[1].equals("true")) {
                        System.out.println("Gained access!");
                    } else {
                        System.out.println("Access denied!");
                    }
                }

                input = in.readLine();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
