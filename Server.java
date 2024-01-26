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
    private String path = "./files";

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

                if (scan.substring(0, 3).equals("CWD")) {
                    if (this.changeDir(scan.substring(4))) {
                        output.write("250 Requested file action okay, completed\r\n".getBytes());
                    } else {
                        output.write("501 Syntax error in parameters or arguments\r\n".getBytes());
                    }
                }
                else if (scan.substring(0, 4).equals("USER")) {
                    if (scan.substring(5).equals("test")) {
                        output.write("331 username ok\r\n".getBytes());
                    } else {
                        // refuser connection car username invalide
                    }
                }
                else if (scan.substring(0, 4).equals("PASS")) {
                    if (scan.substring(5).equals("test")) {
                        output.write("230 user logged in\r\n".getBytes());
                    } else {
                        output.write("530 not logged in\r\n".getBytes());
                        scanner.close();
                        keepInteract = false;
                    }
                }
                else if (scan.substring(0, 4).equals("SYST")) {
                    output.write("215 NAME system type\r\n".getBytes());
                }
                else if (scan.substring(0, 4).equals("FEAT")) {
                    output.write("\r\n".getBytes());
                }
                else if (scan.substring(0, 4).equals("TYPE")) {
                    output.write("200 Command okay\r\n".getBytes());
                }
                else if (scan.substring(0, 4).equals("SIZE")) {
                    File file = this.getFile(scan.substring(5));
                    String resp;
                    if (file != null) {
                        resp  = "213 " + file.length() + "\r\n";
                        output.write(resp.getBytes());
                    } else {
                        output.write("213 File status\r\n".getBytes());
                    }
                }
                else if (scan.substring(0, 4).equals("EPSV")) {
                    String resp = "229 Entering Extended Passive Mode (|||" + this.dataServerSocket.getLocalPort() + "|)\r\n";
                    output.write(resp.getBytes());
                }
                else if (scan.substring(0, 4).equals("LIST")) {
                    String resp = "";
                    String currentPath = this.path;
                    File file;
                    // When we want to display another directory
                    if (scan.length() > 5) {
                        // If the directory doesn't exist
                        if (!this.changeDir(scan.substring(5))) {
                            output.write("501 Syntax error in parameters or arguments\r\n".getBytes());
                        } else {
                            this.dataServer = this.dataServerSocket.accept();
                            output.write("150 Accepted data connection\r\n".getBytes());
                            file = new File (this.path);
                            for (int i = 0; i < file.listFiles().length; i++) {
                                resp = file.listFiles()[i].getName() + "\r\n";
                                this.dataServer.getOutputStream().write(resp.getBytes());
                            }
                            this.dataServer.close();
                            output.write("226 Closing data connection\r\n".getBytes());
                            this.changeDir(currentPath);
                        }
                    } else {
                        this.dataServer = this.dataServerSocket.accept();
                        output.write("150 Accepted data connection\r\n".getBytes());
                        file = new File (this.path);
                        for (int i = 0; i < file.listFiles().length; i++) {
                            resp = file.listFiles()[i].getName() + "\r\n";
                            this.dataServer.getOutputStream().write(resp.getBytes());
                        }
                        this.dataServer.close();
                        output.write("226 Closing data connection\r\n".getBytes());
                    }
                }
                else if (scan.substring(0, 4).equals("RETR")) {
                    File file = this.getFile(scan.substring(5));
                    // If the file does exist
                    if (file != null) {
                        this.dataServer = this.dataServerSocket.accept();
                        String resp = "";
                        output.write("150 Accepted data connection\r\n".getBytes());
                        FileInputStream fileInput = new FileInputStream(file);
                        int b = fileInput.read();
                        while (b != -1) {
                            resp = b + "\r\n";
                            this.dataServer.getOutputStream().write(resp.getBytes());
                            b = fileInput.read();
                        }
                        fileInput.close();
                        this.dataServer.close();
                        output.write("226 File successfully transfered\r\n".getBytes());
                    } else {
                        output.write("501 Syntax error in parameters or arguments\r\n".getBytes());
                    }
                }
                else if (scan.substring(0, 4).equals("MDTM")) {
                    // Change file date
                    output.write("253 Date/time changed okay.\r\n".getBytes());
                }
                else if (scan.substring(0, 4).equals("QUIT")) {
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
        file = new File (this.path + "/" + name);
        // Should also count .. and . to check that we're not in a directory above files
        if (file.exists() && file.isFile()) {
            return file;
        }
        return null;
    }

    public boolean changeDir (String path) {
        File file = new File (this.path + "/" + path);
        if (file.isDirectory()) {
            this.path = this.path + "/" + path;
            return true;
        }
        return false;
    }

    public static void main (String [] args) {
        Server server = new Server();
        while (true) {
            server.listen();
        }
    }

}