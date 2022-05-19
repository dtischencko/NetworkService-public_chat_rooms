package SERVER.PROCESSING;

import SERVER.RESOURCES.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.sql.*;
import java.util.concurrent.RecursiveTask;

public class Registration extends RecursiveTask<Boolean> {

    private String userName;
    private String password;
    private Socket socket;
    private ObjectOutputStream outOb;
    private ObjectInputStream inOb;
    public volatile ArrayList<User> allUsers;
    private ClientHandler clientHandler;

    public Registration(String userName, String password, Socket socket, ObjectOutputStream outOb, ObjectInputStream inOb, ArrayList<User> allUsers, ClientHandler clientHandler) {
        this.userName = userName;
        this.password = password;
        this.socket = socket;
        this.outOb = outOb;
        this.inOb = inOb;
        this.allUsers = allUsers;
        this.clientHandler = clientHandler;
    }

    @Override
    protected Boolean compute() {
        boolean successRegistration = false;
        boolean newUser = true;
        boolean userExist = false;
        Connection connection = null;
        for (User u :
                allUsers) {
            if (userName.equals(u.getUserName())) {
                newUser = false;
                break;
            }
        }
        if (newUser) {
            try {
                connection = DriverManager.getConnection("jdbc:oracle:thin:C##DITY/mypassword@localhost:1521:orcl");
                if (connection != null) {
                    System.out.println("Successful connection to database!" + userName + ":" + password);
                    Statement statement = connection.createStatement();
                    ResultSet query = statement.executeQuery("SELECT * from USER_ACCOUNT");
                    // TODO: ПРОВЕРКА НА ПУСТОТУ БАЗ ДАННЫХ
                    while (query.next()) {
                        String dbU = query.getString(1);
                        if (dbU.equals(userName)) {
                            System.out.println("CHECK SERVER: " + dbU + ":" + userName);
                            userExist = true;
                            break;
                        }
                    }
                    System.out.println(userExist);
                    if (!userExist) {
                        User user;
                        statement.execute("INSERT INTO USER_ACCOUNT (USER_NAME, USER_PASSWORD) VALUES ('" + userName + "', '" + password + "')");
                        allUsers.add(user = new User(userName, socket, outOb, inOb));
                        System.out.println("IN REG" + "\n" + allUsers);
                        clientHandler.setUser(user);
                        successRegistration = true;
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
        }
        else System.out.println("SERVER(REGISTRATION): this user exists already");
        return successRegistration;
    }
}
