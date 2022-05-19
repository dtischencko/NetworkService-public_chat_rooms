package SERVER.RESOURCES;

import java.io.Serializable;

public class Packet implements Serializable {

    private PacketType packetType;
    private String userName;
    private String toUserName;
    private String userPassword;
    private String message = null;
    private int roomID;
    private String[] uir;
    private boolean success;
    private boolean isDbEmpty;
    private boolean isCreate;

    public Packet(PacketType packetType) {
        this.packetType = packetType;
    }

    public Packet(PacketType packetType, String userName, boolean success, boolean isDbEmpty) {
        this.packetType = packetType;
        this.success = success;
        this.userName = userName;
        this.isDbEmpty = isDbEmpty;
    }

    public Packet(PacketType packetType, String userName) {
        this.packetType = packetType;
        this.userName = userName;
    }

    public Packet(PacketType packetType, int roomID, boolean isCreate) {
        this.packetType = packetType;
        this.roomID = roomID;
        this.isCreate = isCreate;
    }

    public Packet(PacketType packetType, String userName, String userPassword) {
        this.packetType = packetType;
        this.userPassword = userPassword;
        this.userName = userName;
    }

    public Packet(PacketType packetType, String userName, int roomID) {
        this.packetType = packetType;
        this.userName = userName;
        this.roomID = roomID;
    }

    public Packet(PacketType packetType, String userName, int roomID, String message) {
        this.packetType = packetType;
        this.userName = userName;
        this.message = message;
        this.roomID = roomID;

    }

    public Packet(PacketType packetType, String userName, int roomID, String message, String[] uir) {
        this.packetType = packetType;
        this.userName = userName;
        this.message = message;
        this.roomID = roomID;
        this.uir = uir;

    }
    public Packet(PacketType packetType, String userName, String toUserName, int roomID, String message) {
        this.packetType = packetType;
        this.userName = userName;
        this.message = message;
        this.roomID = roomID;
        this.toUserName = toUserName;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getRoomID() {
        return roomID;
    }

    public PacketType getPacketType() {
        return packetType;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getMessage() {
        return message;
    }

    public String getToUserName() {
        return toUserName;
    }

    public String[] getUir() {
        return uir;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public boolean isDbEmpty() {
        return isDbEmpty;
    }

    public boolean isCreate() {
        return isCreate;
    }
}
