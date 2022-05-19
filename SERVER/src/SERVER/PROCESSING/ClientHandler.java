package SERVER.PROCESSING;

import SERVER.RESOURCES.Packet;
import SERVER.RESOURCES.PacketType;
import SERVER.RESOURCES.Room;
import SERVER.RESOURCES.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ClientHandler extends RecursiveAction {

    private Socket client;
    public volatile ArrayList<Room> rooms;
    public volatile ArrayList<User> allUsers;
    private ObjectInputStream inOb = null;
    private ObjectOutputStream outOb = null;
    public volatile Map<String, ArrayList<Integer>> dbUsers; // Data about users from database
    public volatile Map<Integer, ArrayList<String>> dbRooms; // Data about rooms from database
    private RequestHandler requestHandler;
    private boolean isConnected = true;

    private volatile User user;

    public ClientHandler(Socket client, ObjectOutputStream outOb, ObjectInputStream inOb, ArrayList<Room> rooms, ArrayList<User> allUsers, Map<String, ArrayList<Integer>> dbUsers, Map<Integer, ArrayList<String>> dbRooms, RequestHandler requestHandler) {
        this.client = client;
        this.rooms = rooms;
        this.allUsers = allUsers;
        this.outOb = outOb;
        this.inOb = inOb;
        this.requestHandler = requestHandler;
        this.dbUsers = dbUsers;
        this.dbRooms = dbRooms;
    }

    @Override
    protected void compute() {

        try {
            do {
                System.out.println("SERVER: waiting input");
                Packet requestPacket = (Packet) inOb.readObject();
                System.out.println("SERVER: " + requestPacket.getPacketType() + " " + requestPacket.getUserName() + " " + requestPacket.getRoomID() + " " + requestPacket.getMessage());
                System.out.println("SERVER: input success");
                switch (requestPacket.getPacketType()) {
                    case LOGIN -> {
                        System.out.println("LOGIN");
                        requestHandler.updateInfoFromDB(dbUsers);
                        requestHandler.updateRoomsInfoFromDB(dbRooms);
                        boolean successLogin = ForkJoinPool.commonPool().invoke(new LoginProcess(requestPacket.getUserName(), requestPacket.getUserPassword(), client, outOb, inOb, allUsers, this));
                        System.out.println(successLogin);

                        Packet answerPacket = new Packet(PacketType.LOGIN, requestPacket.getUserName(), successLogin, dbUsers.isEmpty());
                        outOb.writeObject(answerPacket);
                        outOb.flush();
                        outOb.reset();
                        System.out.println((successLogin && dbUsers.containsKey(requestPacket.getUserName())) + " TEST!!!");
                        if (successLogin && dbUsers.containsKey(requestPacket.getUserName())) {
                            System.out.println("SERVER: START LOAD DBUSERS");
                            ArrayList<Integer> addingRooms = dbUsers.get(requestPacket.getUserName());
                            User dbUser = null;
                            for (User u:
                                    allUsers) {
                                if (Objects.equals(u.getUserName(), requestPacket.getUserName())) {
                                    dbUser = u;
                                    break;
                                }
                            }
                            for (Integer rid: // ALL ROOMS WHETHER THIS USER EXISTS
                                 addingRooms) {
                                Room needRoom = null;
                                int posRid = (rid < 0) ? rid*(-1) : rid;
                                for (Room room:
                                     rooms) {
                                    if (room.getRoomID() == posRid) {
                                        needRoom = room;
                                        break;
                                    }
                                }
                                if (needRoom == null) { // CREATE NEW ROOM IF IT DOES NOT EXIST
                                    Room newRoom = new Room(posRid, dbUser, rid<0);
                                    rooms.add(newRoom);
                                } else { // ADDING USER TO EXISTING ROOM
                                    needRoom.getUsersInRoom().add(dbUser);
                                    if (rid < 0) needRoom.setHost(dbUser);
                                }
                                System.out.println("SERVER(DB_ADD_ROOMS): room #" + posRid + "\nuser " + dbUser + " has added!");
                            }
                            if (!dbUsers.isEmpty()) {
                                System.out.println("SERVER(LOGIN): sending data users to client...");
                                Map<String, ArrayList<Integer>> roomsInfo = dbUsers;
                                outOb.writeObject(roomsInfo);
                                outOb.flush();
                                outOb.reset();

                                System.out.println("SERVER(LOGIN): sending data rooms to client...");
                                Map<Integer, ArrayList<String>> usersInfo = dbRooms;
                                outOb.writeObject(usersInfo);
                                outOb.flush();
                                outOb.reset();

                                System.out.println("SERVER(LOGIN): sending all rooms info");
                            }
                        }
                        requestHandler.updateInfoFromDB(dbUsers);
                        requestHandler.updateRoomsInfoFromDB(dbRooms);
                    }
                    case REGISTRATION -> {
                        System.out.println("REGISTRATION");
                        requestHandler.updateInfoFromDB(dbUsers);
                        boolean successRegistration = ForkJoinPool.commonPool().invoke(new Registration(requestPacket.getUserName(), requestPacket.getUserPassword(), client, outOb, inOb, allUsers, this));
                        System.out.println(successRegistration);
                        Packet answerPacket = new Packet(PacketType.REGISTRATION, requestPacket.getUserName(), successRegistration, true);
                        outOb.writeObject(answerPacket);
                        outOb.flush();
                        outOb.reset();
                        requestHandler.updateInfoFromDB(dbUsers);
                    }
                    case MESSAGE -> {
                        boolean existRoom = false;
                        Room room = null;
                        System.out.println("DEFAULT HERE " + rooms.isEmpty());
                        for (Room r : rooms) {
                            System.out.println("SERVER(CHECK_ROOM_EXISTS): " + r.toString() + "\nCheck user in room: " + allUsers);
                            if (r.getUsersInRoom().contains(user) && r.getRoomID() == requestPacket.getRoomID()) {
                                existRoom = true;
                                room = r;
                                break;
                            }
                        }
                        System.out.println("SERVER: #" + requestPacket.getRoomID() + " isExist on SERVER: " + existRoom);
                        switch (requestPacket.getPacketType()) {
                            case MESSAGE -> {
                                System.out.println("MESSAGE to #" + requestPacket.getRoomID());
                                if (existRoom) {
                                    ForkJoinPool.commonPool().execute(new UserRoomHandler(requestPacket.getUserName(), requestPacket.getRoomID(), requestPacket.getMessage(), room.getUsersInRoom(), PacketType.MESSAGE));
                                    System.out.println("SERVER: message has sent");
                                }
                            }
                            case PRIVATE_MESSAGE -> {
                                System.out.println("MESSAGE from " + requestPacket.getUserName() + " to " + requestPacket.getToUserName() + " in #" + requestPacket.getRoomID());
                                if (existRoom) {
                                    ForkJoinPool.commonPool().execute(new UserRoomHandler(requestPacket.getUserName(), requestPacket.getToUserName(), requestPacket.getRoomID(), requestPacket.getMessage(), room.getUsersInRoom(), PacketType.PRIVATE_MESSAGE));
                                    System.out.println("SERVER: message has sent");
                                }
                            }
                        }
                        requestHandler.updateInfoFromDB(dbUsers);
                        requestHandler.updateRoomsInfoFromDB(dbRooms);
                    }
                    case ROOM_CREATE -> {
                        System.out.println(requestPacket.getUserName() +"\n" + allUsers);
                        for (User u:
                             allUsers) {
                            if (u.getUserName().equals(requestPacket.getUserName())) user = u;
                        }
                        System.out.println("ROOM_CREATE");
                        RoomCreating roomCreating;
                        ForkJoinPool.commonPool().execute(roomCreating = new RoomCreating(rooms, user, allUsers)); //Создаем комнату и добавляем туда создателя
                        Integer roomID = roomCreating.join();
                        System.out.println("SERVER: new user:\n" + user + allUsers.toString());

                        Packet answerPacket = new Packet(PacketType.ROOM_CREATE, requestPacket.getUserName(), roomID); //Отправляем данные обратно клиенту, для генерации комнаты
                        outOb.writeObject(answerPacket);
                        outOb.flush();
                        outOb.reset();
                        requestHandler.updateInfoFromDB(dbUsers);
                        requestHandler.updateRoomsInfoFromDB(dbRooms);

                        // SENDING INFO ABOUT ROOM TO ALL USERS
                        for (User u:
                             allUsers) {
                            Packet infoPacket = new Packet(PacketType.ROOM_UPDATE, roomID, true);
                            if (u.getUserName().equals(requestPacket.getUserName())) continue;
                            ObjectOutputStream userOutOb = u.getOutOb();
                            userOutOb.writeObject(infoPacket);
                            userOutOb.flush();
                            userOutOb.reset();
                        }
                    }
                    case ROOM_CONNECT -> {
                        System.out.println("ROOM_CONNECT");
                        System.out.println("SERVER(ROOM_CONNECT): for user " + requestPacket.getUserName());
                        for (User u:
                                allUsers) {
                            if (u.getUserName().equals(requestPacket.getUserName())) user = u;
                        }
                        System.out.println(rooms.toString());
                        Packet answerPacket = null;
                        if (rooms.isEmpty()) {
                            System.out.println("SERVER: Any rooms don`t exist");
                            answerPacket = new Packet(PacketType.ROOM_CONNECT, requestPacket.getUserName(), requestPacket.getRoomID(), "CANTCONNECT");
                        }
                        else {
                            boolean canConnect = false;
                            RoomConnecting roomConnecting;
                            ForkJoinPool.commonPool().execute(roomConnecting = new RoomConnecting(rooms, user, requestPacket.getRoomID(), allUsers));
                            canConnect = roomConnecting.join();
                            if (canConnect) {
                                System.out.println("SERVER(ROOM_CONNECT): can connect ");
                                for (Room r:
                                     rooms) {
                                    if (r.getRoomID() == requestPacket.getRoomID()) {
                                        ArrayList<User> usersInRoom = r.getUsersInRoom();
                                        String[] uir = new String[usersInRoom.size()];
                                        int i = 0;
                                        for (User u:
                                             usersInRoom) {
                                            uir[i] = u.getUserName();
                                            i++;
                                        }
                                        System.out.println("SERVER-LIST: " + Arrays.toString(uir));
                                        for (User u:
                                             usersInRoom) {
                                            if (u.getUserName().equals(requestPacket.getUserName())) continue;
                                            answerPacket = new Packet(PacketType.ROOM_CONNECT, requestPacket.getUserName(), requestPacket.getRoomID(), "USERS");
                                            ObjectOutputStream uOutOb = u.getOutOb();
                                            uOutOb.writeObject(answerPacket);
                                            uOutOb.flush();
                                            uOutOb.reset();
                                        }
                                        answerPacket = new Packet(PacketType.ROOM_CONNECT, requestPacket.getUserName(), requestPacket.getRoomID(), "CANCONNECT", uir);
                                        break;
                                    }
                                }
                            }
                            else {
                                answerPacket = new Packet(PacketType.ROOM_CONNECT, requestPacket.getUserName(), requestPacket.getRoomID(), "CANTCONNECT");
                            }
                        }
                        System.out.println("SERVER: new user:\n" + user + allUsers.toString());
                        outOb.writeObject(answerPacket);
                        outOb.flush();
                        outOb.reset();
                        System.out.println("SERVER(ROOM_CONNECT): answer has sent");
                        requestHandler.updateInfoFromDB(dbUsers);
                        requestHandler.updateRoomsInfoFromDB(dbRooms);
                    }
                    case ROOM_DELETE -> {
                        System.out.println("before delete " + rooms);
                        Connection connection = null;
                        connection = DriverManager.getConnection("jdbc:oracle:thin:C##DITY/mypassword@localhost:1521:orcl");
                        if (connection != null) {
                            for (User u :
                                    allUsers) {
                                if (u.getUserName().equals(requestPacket.getUserName())) {
                                    ArrayList<Room> Ur = u.getURooms();
                                    Room needR = null;
                                    for (Room r :
                                            Ur) {
                                        if (r.getRoomID() == requestPacket.getRoomID()) {
                                            needR = r;
                                            break;
                                        }
                                    }
                                    if (needR != null) {
                                        Ur.remove(needR);
                                        needR.getUsersInRoom().remove(u);
                                        for (User user :
                                                needR.getUsersInRoom()) {
                                            if (user.getUserName().equals(requestPacket.getUserName())) continue;
                                            ObjectOutputStream uOutOb = user.getOutOb();
                                            Packet answerPacket = new Packet(PacketType.ROOM_DELETE, requestPacket.getUserName(), needR.getRoomID(), "DUIR");
                                            uOutOb.writeObject(answerPacket);
                                            uOutOb.flush();
                                            uOutOb.reset();
                                        }
                                        Statement statement = connection.createStatement();
                                        if (Objects.equals(requestPacket.getUserName(), needR.getHost().getUserName())) {
                                            System.out.println("SERVER(DELETE): REQUEST FROM OWNER " + needR.getRoomID());
                                            statement.executeUpdate("DELETE FROM USER_ROOMS WHERE ROOM_ID = '" + needR.getRoomID() + "'");
                                            statement.executeUpdate("DELETE FROM USER_ROOMS WHERE ROOM_ID = '" + (needR.getRoomID())*(-1) + "'");
                                            for (User user :
                                                    needR.getUsersInRoom()) {
                                                user.getURooms().remove(needR);
                                                ObjectOutputStream uOutOb = user.getOutOb();
                                                Packet answerPacket = new Packet(PacketType.ROOM_DELETE, needR.getRoomID(), false);
                                                uOutOb.writeObject(answerPacket);
                                                uOutOb.flush();
                                                uOutOb.reset();

                                                Packet infoPacket = new Packet(PacketType.ROOM_UPDATE, needR.getRoomID(), false);
                                                uOutOb.writeObject(infoPacket);
                                                uOutOb.flush();
                                                uOutOb.reset();
                                            }
                                            rooms.remove(needR);
                                        } else {
                                            System.out.println("SERVER(DELETE): REQUEST FROM NOT OWNER");
                                            int posRoom;
                                            if (needR.getRoomID() < 0) posRoom = needR.getRoomID()*(-1);
                                            else posRoom = needR.getRoomID();
                                            statement.executeUpdate("DELETE FROM USER_ROOMS WHERE USER_NAME = '" + u.getUserName() + "' AND ROOM_ID = '" + posRoom + "'");
                                        }
                                    }
                                }
                            }
                            connection.close();
                            System.out.println("USERS + ROOMS: " + dbUsers);
                        }
                        System.out.println("after delete " + rooms);
                        requestHandler.updateInfoFromDB(dbUsers);
                        requestHandler.updateRoomsInfoFromDB(dbRooms);
                    }
                    case EXIT -> {
                        System.out.println("SERVER(EXIT) " + requestPacket.getUserName());
                        Packet answerPacket = new Packet(PacketType.EXIT);
                        outOb.writeObject(answerPacket);
                        outOb.flush();
                        outOb.reset();
                        user.getInOb().close();
                        user.getOutOb().close();
                        user.getSocket().close();
                        allUsers.remove(user);
                        System.out.println("SERVER SUCCESSFUL HAS DELETED USER " + requestPacket.getUserName());
                        isConnected = false;
                    }

                }
            } while (isConnected);
        } catch (IOException | ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }


    }

    public void setUser(User user) {
        this.user = user;
    }
}
