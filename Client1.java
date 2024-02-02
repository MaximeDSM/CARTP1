import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class Client1 {
    
    private Socket messageSocket;
    private Socket dataSocket;
    private String ip;
    private int port;

    public Client1 (String ip, int port) {
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
        try {
            System.out.println(scanner.nextLine());
            output.write("USER miage\r\n".getBytes());
            System.out.println(scanner.nextLine());
            output.write("PASS car\r\n".getBytes());
            System.out.println(scanner.nextLine());
            output.write("QUIT\r\n".getBytes());
            System.out.println(scanner.nextLine());
            scanner.close();
        } catch (IOException exc) {
            System.out.println(exc);
        }
    }

    public static void main (String [] args) {
        Client1 client = new Client1("localhost", 2121);
    }

}
