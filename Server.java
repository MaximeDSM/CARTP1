import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;

public class Server {

    private int port = 2121;
    private ServerSocket server;
    private ServerSocket dataServerSocket;
    private Socket serverAccept;
    private Socket dataServer;
    // mode 0 = ascii
    // mode 1 = binaire
    private int writingMode = 0;

    public Server () {
        try {
            this.server = new ServerSocket(this.port);
            this.dataServerSocket = new ServerSocket(0);
            System.out.println("Serveur prêt à accepter des connexions sur le port " + this.port);
            this.serverAccept = this.server.accept();
        } catch (IOException exc) {
            System.out.println(exc.toString());
        }
    }

    public int listen () {
        try {
            if (this.serverAccept.isClosed()) {
                this.serverAccept = this.server.accept();
            }
            if (serverAccept.getInputStream() != null) {
                this.interact(serverAccept.getInputStream(), serverAccept.getOutputStream());
            }
        } catch (IOException exc) {
            System.out.println("passe");
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
                        output.write("530 not logged in\r\n".getBytes());
                        scanner.close();
                        keepInteract = false;
                    }
                }
                if (scan.substring(0, 4).equals("SYST")) {
                    output.write("215 NAME system type\r\n".getBytes());
                }
                if (scan.substring(0, 4).equals("FEAT")) {
                    output.write("\r\n".getBytes());
                }
                if (scan.substring(0, 4).equals("TYPE")) {
                    if (scan.charAt(5) == 'I') {
                        this.writingMode = 1;
                    } else if (scan.charAt(5) == 'A') {
                        this.writingMode = 0;
                    }
                    output.write("200 Command okay\r\n".getBytes());
                }
                if (scan.substring(0, 4).equals("SIZE")) {
                    File file = this.getFile(scan.substring(5));
                    String resp;
                    if (file != null) {
                        resp  = "213 " + file.length() + "\r\n";
                        output.write(resp.getBytes());
                    }
                }
                if (scan.substring(0, 4).equals("EPSV")) {
                    String resp = "229 Entering Extended Passive Mode (|||" + this.dataServerSocket.getLocalPort() + "|)\r\n";
                    output.write(resp.getBytes());
                }
                if (scan.substring(0, 4).equals("RETR")) {
                    this.dataServer = this.dataServerSocket.accept();
                    File file = this.getFile(scan.substring(5));
                    FileInputStream fileInput = new FileInputStream(file);
                    String resp = "";
                    output.write("150 Accepted data connection\r\n".getBytes());
                    int b = fileInput.read();
                    while (b != -1) {
                        resp = b + "\r\n";
                        this.dataServer.getOutputStream().write(resp.getBytes());
                        b = fileInput.read();
                    }
                    this.dataServer.close();
                    fileInput.close();
                    System.out.println("passe");
                    output.write("226 File successfully transfered\r\n".getBytes());
                }
                if (scan.substring(0, 4).equals("MDTM")) {
                    output.write("421 Service not available, closing control connection.\r\n".getBytes());
                    scanner.close();
                    keepInteract = false;
                }
                if (scan.substring(0, 4).equals("QUIT")) {
                    output.write("221 Service closing control connection\r\n".getBytes());
                    scanner.close();
                    keepInteract = false;
                }
            }
        } catch (IOException exc) {
            System.out.println(exc.toString());
        }
    }

    public File getFile (String name) {
        File file;
        file = new File ("./files/" + name);
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public static void main (String [] args) {
        Server server = new Server();
        while (true) {
            server.listen();
        }
    }

}