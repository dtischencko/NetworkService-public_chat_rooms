package SERVER.RESOURCES;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class User {

    private volatile ArrayList<Room> uRooms;
    private String userName;
    private Socket socket;
    private ObjectOutputStream outOb;
    private ObjectInputStream inOb;

    public User(String userName, Socket socket, ObjectOutputStream outOb, ObjectInputStream inOb) {
        uRooms = new ArrayList<>();
        this.userName = userName;
        this.socket = socket;
        this.outOb = outOb;
        this.inOb = inOb;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setOutOb(ObjectOutputStream outOb) {
        this.outOb = outOb;
    }

    public void setInOb(ObjectInputStream inOb) {
        this.inOb = inOb;
    }

    public ArrayList<Room> getURooms() {
        return uRooms;
    }

    public String getUserName() {
        return userName;
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectOutputStream getOutOb() {
        return outOb;
    }

    public ObjectInputStream getInOb() {
        return inOb;
    }

    public void addRoom(Room room) {
        uRooms.add(room);
    }

    @Override
    public String toString() {
        String str = "User:\n UserName = " + userName + "\n User`s rooms: ";
        if (!uRooms.isEmpty()) {
            for (Room r:
                 uRooms) {
                str += r.getRoomID() + " ";
            }
            str += "\n";
            return str;
        }
        return str + "none\n";
    }
}