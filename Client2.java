import java.io.IOException;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class Client2 {
    
    private Socket messageSocket;
    private Socket dataSocket;
    private String ip;
    private int port;

    public Client2 (String ip, int port) {
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
        boolean waitingResponse = true;
        try {
            System.out.println(scanner.nextLine());
            output.write("USER miage\r\n".getBytes());
            System.out.println(scanner.nextLine());
            output.write("PASS car\r\n".getBytes());
            System.out.println(scanner.nextLine());

            output.write("PING\r\n".getBytes());
            System.out.println(scanner.nextLine());
            while (waitingResponse) {
                String scan = scanner.nextLine();
                if (scan.equals("PONG")) {
                    output.write("200 PONG command ok\r\n".getBytes());
                    waitingResponse = false;
                } else {
                    output.write("502 unknown command\r\n".getBytes());
                    waitingResponse = false;
                }
            }

            output.write("QUIT\r\n".getBytes());
            System.out.println(scanner.nextLine());
            scanner.close();
        } catch (IOException exc) {
            System.out.println(exc);
        }
    }

    public static void main (String [] args) {
        Client2 client = new Client2("localhost", 2121);
    }

}