package SERVER.PROCESSING;

import SERVER.RESOURCES.Room;
import SERVER.RESOURCES.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;

public class RoomCreating extends RecursiveTask<Integer> {

    public volatile ArrayList<Room> rooms;
    public volatile ArrayList<User> allUsers;
    private int roomID;

    public RoomCreating(ArrayList<Room> rooms, User user, ArrayList<User> allUsers) {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:C##DITY/mypassword@localhost:1521:orcl");
            if (connection != null) {
                System.out.println("Successful connection to database!");
                Statement statement = connection.createStatement();
                System.out.println("Room is Creating" + user);
                this.rooms = rooms;
                this.allUsers = allUsers;
                roomID = (int)(Math.random()*1000000);
                Room room = new Room(roomID, user, true);
                rooms.add(room);
                statement.execute("INSERT INTO USER_ROOMS (USER_NAME, ROOM_ID) VALUES ('" + user.getUserName() + "', '" + (roomID*(-1)) + "')");
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
    }

    @Override
    protected Integer compute() {
        return roomID;
    }
}
