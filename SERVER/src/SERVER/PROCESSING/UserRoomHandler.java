package SERVER.PROCESSING;

import SERVER.RESOURCES.Packet;
import SERVER.RESOURCES.PacketType;
import SERVER.RESOURCES.User;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.concurrent.RecursiveAction;

public class UserRoomHandler extends RecursiveAction {

    private String userName;
    private String toUserName;
    private int roomID;
    private String message = "";
    private PacketType messageType;
    private volatile ArrayList<User> usersInRoom;
    private ObjectOutputStream outOb;

    public UserRoomHandler(String userName, int roomID, String message, ArrayList<User> usersInRoom, PacketType messageType) {
        this.userName = userName;
        this.roomID = roomID;
        this.message = message;
        this.messageType = messageType;
        this.usersInRoom = usersInRoom;
    }

    public UserRoomHandler(String userName, String toUserName, int roomID, String message, ArrayList<User> usersInRoom, PacketType messageType) {
        this.userName = userName;
        this.roomID = roomID;
        this.message = message;
        this.messageType = messageType;
        this.usersInRoom = usersInRoom;
        this.toUserName = toUserName;
    }

    @Override
    protected void compute() {
        switch (messageType) {
            case MESSAGE -> {
                for (User u:
                     usersInRoom) {
                    outOb = u.getOutOb();
                    Packet packet = new Packet(PacketType.MESSAGE, userName, roomID, message);
                    try {
                        System.out.println("SERVER: start packing message from " + userName + " in #" + roomID + " room");
                        outOb.writeObject(packet);
                        outOb.flush();
                        outOb.reset();
                        System.out.println("SERVER: message send to " + u.getUserName());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            case PRIVATE_MESSAGE -> {
                for (User u:
                        usersInRoom) {
                    if (u.getUserName().equals(toUserName) || u.getUserName().equals(userName)) {
                        outOb = u.getOutOb();
                        Packet packet = new Packet(PacketType.PRIVATE_MESSAGE, userName, toUserName, roomID, message);
                        try {
                            System.out.println("SERVER: start packing (private)message from " + userName + " to "+ toUserName + " in #" + roomID + " room");
                            outOb.writeObject(packet);
                            outOb.flush();
                            outOb.reset();
                            System.out.println("SERVER: message send to " + u.getUserName());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
