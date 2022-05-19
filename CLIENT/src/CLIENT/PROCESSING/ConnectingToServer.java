package CLIENT.PROCESSING;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.RecursiveAction;

public class ConnectingToServer extends RecursiveAction {

    public static Socket userSocket;
    public static ObjectOutputStream outOb;
    public static ObjectInputStream inOb;
    private final String SERVER_IP;
    private final int SERVER_PORT = 9980;

    public ConnectingToServer(String textIP) {
        this.SERVER_IP = textIP;
    }

    @Override
    protected void compute() {
        try {
            userSocket = new Socket(SERVER_IP, SERVER_PORT);
            outOb = new ObjectOutputStream(userSocket.getOutputStream());
            inOb = new ObjectInputStream(userSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("CLIENT: SUCCESSFUL CONNECTION!");
    }
}
