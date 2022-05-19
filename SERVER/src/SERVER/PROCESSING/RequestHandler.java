package SERVER.PROCESSING;

import SERVER.RESOURCES.Room;
import SERVER.RESOURCES.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class RequestHandler extends RecursiveAction {

    private ServerSocket serverSocket;
    private final int PORT = 9980;
    private Socket requestSocket;
    public volatile ArrayList<Room> rooms;
    public volatile ArrayList<User> allUsers;
    public volatile Map<String, ArrayList<Integer>> dbUsers;
    public volatile Map<Integer, ArrayList<String>> dbRooms;

    public RequestHandler() throws IOException {

        rooms = new ArrayList<>();
        allUsers = new ArrayList<>();
        dbUsers = new HashMap<>();
        dbRooms = new HashMap<>();
        serverSocket = new ServerSocket(PORT);
        System.out.println("====SERVER HAS STARTED====");
    }

    @Override
    protected void compute() {
        this.updateInfoFromDB(dbUsers);
        this.updateRoomsInfoFromDB(dbRooms);
        try {
            do {
                requestSocket = serverSocket.accept();
                ObjectOutputStream outOb = new ObjectOutputStream(requestSocket.getOutputStream());
                ObjectInputStream inOb = new ObjectInputStream(requestSocket.getInputStream());
                System.out.println("SERVER: new connection");
                ForkJoinPool.commonPool().execute(new ClientHandler(requestSocket, outOb, inOb, rooms, allUsers, dbUsers, dbRooms, this)); // ОТПРАВЛЯЕМ клиент на обработку
            } while (true);
        } catch (IOException e) {
            e.getStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void updateInfoFromDB(Map<String, ArrayList<Integer>> dbUsers) {
        Connection connection = null;
        Statement statement = null;
        ArrayList<String> userNames;
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:C##DITY/mypassword@localhost:1521:orcl");
            if (connection != null) {
                System.out.println("Successful connection to database!");
                userNames = new ArrayList<>();
                statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * from USER_ROOMS");
                while (resultSet.next()) {
                    String dbU = resultSet.getString(1);
                    if (!userNames.contains(dbU)) {
                        userNames.add(dbU);
                    }
                }
                ArrayList<Integer> rid;
                for (String userName : userNames) {
                    resultSet = statement.executeQuery("SELECT * from USER_ROOMS WHERE USER_NAME = '" + userName + "'");
                    rid = new ArrayList<>();
                    while (resultSet.next()) {
                        int dbRID = resultSet.getInt(2);
                        rid.add(dbRID);
                    }
                    dbUsers.put(userName, rid);
                }
                System.out.println("AFTER UPDATE USERS: " + dbUsers.toString());
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                assert statement != null;
                statement.close();
                connection.close();
                System.out.println("USER + ROOMS: " + dbUsers);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void updateRoomsInfoFromDB(Map<Integer, ArrayList<String>> dbRooms) {
        Connection connection = null;
        Statement statement = null;
        ArrayList<Integer> ridKeys;
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:C##DITY/mypassword@localhost:1521:orcl");
            if (connection != null) {
                System.out.println("Successful connection to database!");
                ridKeys = new ArrayList<>();
                statement = connection.createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT * from USER_ROOMS");
                while (resultSet.next()) {
                    int dbRid = resultSet.getInt(2);
                    if (dbRid<0) dbRid*=-1;
                    if (!ridKeys.contains(dbRid)) {
                        ridKeys.add(dbRid);
                    }
                }
                ArrayList<String> names;
                for (Integer id : ridKeys) {
                    resultSet = statement.executeQuery("SELECT * from USER_ROOMS WHERE ROOM_ID = " + id);
                    names = new ArrayList<>();
                    while (resultSet.next()) {
                        String dbU = resultSet.getString(1);
                        names.add(dbU);
                    }
                    resultSet = statement.executeQuery("SELECT * from USER_ROOMS WHERE ROOM_ID = " + id*(-1));
                    while (resultSet.next()) {
                        String dbU = resultSet.getString(1);
                        names.add(dbU);
                    }
                    dbRooms.put(id, names);
                }
                System.out.println("AFTER UPDATE ROOMS: " + dbRooms.toString());
            }
        } catch (SQLException throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                assert statement != null;
                statement.close();
                connection.close();
                System.out.println("ROOM + USERS: " + dbRooms);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}