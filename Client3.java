import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class Client3 {
    
    private Socket messageSocket;
    private Socket dataSocket;
    private String ip;
    private int port;

    public Client3 (String ip, int port) {
        try {
            this.messageSocket = new Socket(ip, port);
            this.ip = ip;
            this.port = port;
            this.scenario(messageSocket.getInputStream(), messageSocket.getOutputStream());
        } catch (IOException exc) {
            System.out.println(exc);
        }
    }

    public void scenario (InputStream input, OutputStream output) {
        Scanner scanner = new Scanner(input);
        Scanner dataScanner;
        try {
            System.out.println(scanner.nextLine());
            output.write("USER miage\r\n".getBytes());
            System.out.println(scanner.nextLine());
            output.write("PASS car\r\n".getBytes());
            System.out.println(scanner.nextLine());

            output.write("EPSV\r\n".getBytes());
            String dataPort = scanner.nextLine();
            System.out.println(dataPort);
            dataPort = dataPort.substring(39);
            dataPort = dataPort.substring(0, dataPort.indexOf('|', 0));
            output.write("CWD subfiles\r\n".getBytes());
            System.out.println(scanner.nextLine());
            output.write("LINE test.txt 3\r\n".getBytes());
            System.out.println(scanner.nextLine());
            dataSocket = new Socket("localhost", Integer.parseInt(dataPort));
            dataScanner = new Scanner(dataSocket.getInputStream());
            System.out.println(dataScanner.nextLine());
            dataSocket.close();
            System.out.println(scanner.nextLine());

            output.write("QUIT\r\n".getBytes());
            System.out.println(scanner.nextLine());
            scanner.close();
        } catch (IOException exc) {
            System.out.println(exc);
        }
    }

    public static void main (String [] args) {
        Client3 client = new Client3("localhost", 2121);
    }

}