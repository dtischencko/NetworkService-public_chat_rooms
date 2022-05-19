package CLIENT.PROCESSING;

import CLIENT.GUI.MainFrame;
import SERVER.RESOURCES.Packet;
import SERVER.RESOURCES.PacketType;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.RecursiveAction;

import static CLIENT.PROCESSING.ConnectingToServer.outOb;

public class SendingRequest extends RecursiveAction {

    private MainFrame mainFrame;
    private final PacketType packetType;
    private Socket userSocket;
    private int roomID;
    private String userName = null;
    private String toUserName = null;
    private String userPassword = null;
    private String message = null;

    /** CONSTRUCTOR for ROOM CREATE/EXIT */
    public SendingRequest(MainFrame mainFrame, Socket userSocket, PacketType packetType, String userName) {
        this.packetType = packetType;
        this.userName = userName;
        this.userSocket = userSocket;
        this.mainFrame = mainFrame;
    }

    /** CONSTRUCTOR for LOGIN/REGISTRATION */
    public SendingRequest(MainFrame mainFrame, Socket userSocket, PacketType packetType, String userName, String userPassword) {
        this.packetType = packetType;
        this.userPassword = userPassword;
        this.userName = userName;
        this.userSocket = userSocket;
        this.mainFrame = mainFrame;
    }

    /** CONSTRUCTOR for ROOM CONNECT/DELETE */
    public SendingRequest(MainFrame mainFrame, Socket userSocket, PacketType packetType, String userName, int roomID) {
        this.packetType = packetType;
        this.roomID = roomID;
        this.userName = userName;
        this.userSocket = userSocket;
        this.mainFrame = mainFrame;
    }

    /** CONSTRUCTOR for MESSAGE */
    public SendingRequest(MainFrame mainFrame, Socket userSocket, PacketType packetType, String userName, int roomID, String message) {
        this.packetType = packetType;
        this.roomID = roomID;
        this.userName = userName;
        this.message = message;
        this.userSocket = userSocket;
        this.mainFrame = mainFrame;
    }

    /** CONSTRUCTOR for PRIVATE_MESSAGE */
    public SendingRequest(MainFrame mainFrame, Socket userSocket, PacketType packetType, String userName, String toUserName, int roomID, String message) {
        this.packetType = packetType;
        this.roomID = roomID;
        this.userName = userName;
        this.message = message;
        this.userSocket = userSocket;
        this.mainFrame = mainFrame;
        this.toUserName = toUserName;
    }

    @Override
    protected void compute() {

        try {
            switch (packetType) {
                case MESSAGE -> {
                    System.out.println("MESSAGE");
                    Packet packet = new Packet(PacketType.MESSAGE, userName, roomID, message);
                    outOb.writeObject(packet);
                    outOb.flush();
                    outOb.reset();
                    System.out.println("CLIENT: message has sent by " + userName + " in #" + roomID + " room");
                }
                case PRIVATE_MESSAGE -> {
                    System.out.println("PRIVATE_MESSAGE");

                    //PRIVATE_PROCESSING//

                    Packet packet = new Packet(PacketType.PRIVATE_MESSAGE, userName, toUserName, roomID, message);
                    outOb.writeObject(packet);
                    outOb.flush();
                    outOb.reset();
                }
                case LOGIN -> {
                    System.out.println("LOGIN");
                    Packet packet = new Packet(PacketType.LOGIN, userName, userPassword);
                    outOb.writeObject(packet);
                    outOb.flush();
                    outOb.reset();
                }
                case REGISTRATION -> {
                    System.out.println("REGISTRATION");
                    Packet packet = new Packet(PacketType.REGISTRATION, userName, userPassword);
                    outOb.writeObject(packet);
                    outOb.flush();
                    outOb.reset();
                }
                case ROOM_CREATE -> {
                    System.out.println("ROOM_CREATE");
                    System.out.println("CLIENT: room create");
                    Packet packet = new Packet(PacketType.ROOM_CREATE, userName);  //Отправка запроса на сервер для создания комнаты
                    outOb.writeObject(packet);
                    outOb.flush();
                    outOb.reset();
                    System.out.println("CLIENT(ROOM_CREATE): data has sent");
                }
                case ROOM_CONNECT -> {
                    System.out.println("ROOM_CONNECT");
                    Packet packet = new Packet(PacketType.ROOM_CONNECT, userName, roomID);
                    outOb.writeObject(packet);
                    outOb.flush();
                    outOb.reset();
                    System.out.println("CLIENT(ROOM_CONNECT): data has sent");
                }
                case ROOM_DELETE -> {
                    System.out.println("ROOM_DELETE");
                    Packet packet = new Packet(PacketType.ROOM_DELETE, userName, roomID);
                    outOb.writeObject(packet);
                    outOb.flush();
                    outOb.reset();
                    System.out.println("CLIENT(ROOM_DELETE): data has sent");
                }
                case ROOM_UPDATE -> {
                    System.out.println("CLIENT(ROOM_UPDATE)");
                    //Packet packet = new Packet(PacketType.ROOM_UPDATE, )
                }
                case EXIT -> {
                    System.out.println("CLIENT(EXIT)");
                    Packet packet = new Packet(PacketType.EXIT, userName);
                    outOb.writeObject(packet);
                    outOb.flush();
                    outOb.reset();
                    System.out.println("CLIENT(EXIT): data has sent");
                }
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}
