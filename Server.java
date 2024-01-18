import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Server {

    private int port = 2121;
    private ServerSocket server;
    private Socket serverAccept;

    private boolean connected = false;
    private String username;
    private String password;

    public Server () {
        try {
            this.server = new ServerSocket(this.port);
            System.out.println("Serveur prêt à accepter des connexions sur le port " + this.port);
            this.serverAccept = this.server.accept();
        } catch (IOException exc) {
            System.out.println(exc.toString());
        }
    }

    public int listen () {
        try {
            if (serverAccept.getInputStream() != null) {
                this.interact(serverAccept.getInputStream(), serverAccept.getOutputStream());
            }
        } catch (IOException exc) {
            System.out.println(exc.toString());
        }
        
        return 0;
    }

    public void interact (InputStream input, OutputStream output) {
        boolean keepInteract = true;
        Scanner scanner = new Scanner(input);
        try {
            output.write("220 service ready\r\n".getBytes());
        } catch (IOException exc) {
            System.out.println(exc.toString());
        }
        try {
            while (keepInteract) {
                String scan = scanner.nextLine();
                if (scan.length() >= 4) {
                    System.out.println(scan);
                    if (scan.substring(0, 4).equals("USER")) {
                        if (scan.substring(5).equals("test")) {
                            output.write("331 username ok\r\n".getBytes());
                        } else {
                            // refuser connection car username invalide
                        }
                    }
                    if (scan.substring(0, 4).equals("PASS")) {
                        if (scan.substring(5).equals("test")) {
                            output.write("230 user logged in\r\n".getBytes());
                        } else {
                            // refuser connection car password invalide
                        }
                    }
                    if (scan.substring(0, 4).equals("QUIT")) {
                        output.write("221 Service closing control connection\r\n".getBytes());
                        keepInteract = false;
                    }
                }
            }
            scanner.close();
            input.close();
            output.close();
        } catch (IOException exc) {
            System.out.println(exc.toString());
        }
    }

    public static void main (String [] args) {
        Server server = new Server();
        while (true) {
            server.listen();
        }
    }

}