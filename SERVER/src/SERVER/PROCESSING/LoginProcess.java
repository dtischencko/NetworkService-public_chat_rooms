package SERVER.PROCESSING;

import SERVER.RESOURCES.User;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.concurrent.RecursiveTask;

public class LoginProcess extends RecursiveTask<Boolean> {

    private String userName;
    private String password;
    public volatile ArrayList<User> allUsers;
    private Socket socket;
    private ObjectOutputStream outOb;
    private ObjectInputStream inOb;
    private ClientHandler clientHandler;

    public LoginProcess(String userName, String password, Socket socket, ObjectOutputStream outOb, ObjectInputStream inOb, ArrayList<User> allUsers, ClientHandler clientHandler) {
        this.userName = userName;
        this.password = password;
        this.allUsers = allUsers;
        this.socket = socket;
        this.outOb = outOb;
        this.inOb = inOb;
        this.clientHandler = clientHandler;
    }

    @Override
    protected Boolean compute() {
        Connection connection = null;
        boolean successLogin = false;
        boolean newUser = true;
        try {
            connection = DriverManager.getConnection("jdbc:oracle:thin:C##DITY/mypassword@localhost:1521:orcl");
            if (connection != null) {
                System.out.println("Successful connection to database!" + userName + ":" + password);
                Statement statement = connection.createStatement();
                ResultSet set = statement.executeQuery("SELECT * from USER_ACCOUNT");
                while (set.next()) {
                    String dbU = set.getString(1);
                    String dbP = set.getString(2);
                    System.out.println("DB=>" + dbU + ":" + dbP);
                    System.out.println(userName.equals(dbU) + " AND " + password.equals(dbP));
                    if (userName.equals(dbU) && password.equals(dbP)) {
                        successLogin = true;
                        for (User u :
                                allUsers) {
                            if (userName.equals(u.getUserName())) {
                                newUser = false;
                                break;
                            }
                        }
                        if (newUser) {
                            allUsers.add(new User(userName, socket, outOb, inOb));
                        }
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
        if (successLogin) {
            for (User u:
                 allUsers) {
                if (u.getUserName().equals(userName)) {
                    u.setSocket(socket);
                    u.setOutOb(outOb);
                    u.setInOb(inOb);
                    clientHandler.setUser(u);
                    break;
                }
            }
        }
        return successLogin;
    }
}
