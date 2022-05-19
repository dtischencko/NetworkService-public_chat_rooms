package SERVER.RESOURCES;

import java.util.ArrayList;

public class Room {

    private User host;
    private int roomID;
    private volatile ArrayList<User> usersInRoom;

    public Room(int roomID, User user, boolean isHost) {
        usersInRoom = new ArrayList<>();
        if (isHost) this.host = user;
        this.roomID = roomID;
        user.addRoom(this);
        usersInRoom.add(user);
    }

    public int getRoomID() {
        return roomID;
    }

    public ArrayList<User> getUsersInRoom() {
        return usersInRoom;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return "RoomID: " + roomID + "\nHost: " + host.getUserName() + "\nUIR: " + usersInRoom.toString();
    }
}
