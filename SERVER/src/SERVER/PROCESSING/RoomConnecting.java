package SERVER.PROCESSING;

import SERVER.RESOURCES.Room;
import SERVER.RESOURCES.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;

public class RoomConnecting extends RecursiveTask<Boolean> {

    public volatile ArrayList<Room> rooms;
    public volatile ArrayList<User> allUsers;
    private User user;
    private int roomID;
    private Room connectedRoom = null;
    private boolean canConnect = false;
    private boolean alreadyInRoom = false;

    public RoomConnecting(ArrayList<Room> rooms, User user, int roomID, ArrayList<User> allUsers) {
        this.rooms = rooms;
        this.allUsers = allUsers;
        this.roomID = roomID;
        this.user = user;
    }

    @Override
    protected Boolean compute() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:C##DITY/mypassword@localhost:1521:orcl");
            if (connection != null) {
                for (Room x :
                        rooms) {
                    if (x.getRoomID() == roomID) {
                        connectedRoom = x;
                        canConnect = true;
                        for (User u :
                                connectedRoom.getUsersInRoom()) {
                            if (u == user) {
                                alreadyInRoom = true;
                                break;
                            }
                        }
                        if (!alreadyInRoom) {

                            System.out.println("Successful connection to database!");
                            Statement statement = connection.createStatement();
                            System.out.println("User is connecting" + user);
                            statement.execute("INSERT INTO USER_ROOMS (USER_NAME, ROOM_ID) VALUES ('" + user.getUserName() + "', '" + (roomID) + "')");
                        }
                        connectedRoom.getUsersInRoom().add(user);
                        user.addRoom(connectedRoom);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                assert connection != null;
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return connectedRoom != null && canConnect;
    }
}
