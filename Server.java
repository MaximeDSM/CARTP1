import java.net.Socket;
import java.net.ServerSocket;
import java.util.Scanner;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.*;

public class Server {

    private int port = 2121;
    private ServerSocket server;
    private ServerSocket dataServerSocket;
    private Socket serverAccept;
    private Socket dataServer;
    private String path = "./files";
    private String CDPATH = "./files";

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
                    if (scan.length() > 3) {
                        if (this.changeDir(scan.substring(4))) {
                            output.write("250 Requested file action okay, completed\r\n".getBytes());
                        } else {
                            output.write("501 Syntax error in parameters or arguments\r\n".getBytes());
                        }
                    } else {
                        this.changeDir(this.CDPATH);
                        output.write("250 Requested file action okay, completed\r\n".getBytes());
                    }
                }
                else if (scan.substring(0, 4).equals("USER")) {
                    if (scan.substring(5).equals("miage")) {
                        output.write("331 username ok\r\n".getBytes());
                    } else {
                        // refuser connection car username invalide
                    }
                }
                else if (scan.substring(0, 4).equals("PASS")) {
                    if (scan.substring(5).equals("car")) {
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
                        // If the directory doesn't exist or isn't a directory
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
                        output.write("150 Accepted data connection\r\n".getBytes());
                        FileInputStream fileInput = new FileInputStream(file);
                        byte[] b = new byte[4096];
                        while (fileInput.read(b) != -1) {
                            this.dataServer.getOutputStream().write(b);
                        }
                        fileInput.close();
                        output.write("226 File successfully transfered\r\n".getBytes());
                        this.dataServer.close();
                    } else {
                        output.write("501 Syntax error in parameters or arguments\r\n".getBytes());
                    }
                }
                else if (scan.substring(0, 4).equals("MDTM")) {
                    // Change file date
                    output.write("253 Date/time changed okay.\r\n".getBytes());
                }
                else if (scan.substring(0, 4).equals("PING")) {
                    output.write("200 PING command ok\r\n".getBytes());
                    output.write("PONG\r\n".getBytes());
                }
                else if (scan.substring(0, 4).equals("LINE")) {
                    String fileName = scan.substring(5, scan.indexOf(' ', 5));
                    String lineNum = scan.substring(scan.indexOf(' ', 5) + 1);
                    String line;
                    File file;
                    if ((file = this.getFile(fileName)) != null) {
                        line = this.getFileLine(Integer.parseInt(lineNum) , file);
                        this.dataServer = this.dataServerSocket.accept();
                        output.write("150 Accepted data connection\r\n".getBytes());
                        line = line + "\r\n";
                        System.out.println(line);
                        dataServer.getOutputStream().write(line.getBytes());
                        this.dataServer.close();
                        output.write("226 File successfully transfered\r\n".getBytes());
                    }
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
        if (file.exists() && file.isFile()) {
            return file;
        }
        return null;
    }

    public boolean changeDir (String path) {
        Path normPath;
        String tmpPath = this.path;
        File file = new File (this.path + "/" + path);
        if (file.isDirectory()) {
            this.path = this.path + "/" + path;
            normPath = Paths.get(this.path);
            normPath = normPath.normalize();
            if (!normPath.startsWith("files")) {
                this.path = tmpPath;
                return false;
            }
            return true;
        }
        return false;
    }

    public String getFileLine (int lineNum, File file) {
        String res = null;
        Scanner scanner;
        try {
            int count = 1;
            String line;
            scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                line = scanner.nextLine();
                if (count == lineNum) {
                    res = line;
                }
                count ++;
            }
            scanner.close();
        } catch (FileNotFoundException exc) {
            System.out.println(exc);
        }
        System.out.println(res);
        return res;
    }

    public static void main (String [] args) {
        Server server = new Server();
        while (true) {
            server.listen();
        }
    }

}