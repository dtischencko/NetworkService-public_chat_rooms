package CLIENT.GUI;

import CLIENT.PROCESSING.ConnectingToServer;
import CLIENT.PROCESSING.SendingRequest;
import SERVER.RESOURCES.PacketType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

public class RoomDialog extends JDialog implements ActionListener {

    private int roomID;
    private JTextArea chatArea;
    private JTextField field;
    private String userName;
    private MainFrame mainFrame;
    private JComboBox<String> usersInRooms;
    private String[] uir;

    public RoomDialog(Frame owner, String title, boolean modal, int roomID, String userName, String[] uir) {
        super(owner, title, modal);
        this.roomID = roomID;
        this.userName = userName;
        this.mainFrame = (MainFrame) owner;
        this.uir = uir;
        MainFrame.yourRoomDialogs.add(this);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(new Dimension(400,600));
        setResizable(false);

        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        usersInRooms = new JComboBox<>();
        chatArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        field = new JTextField();
        JButton sendButton = new JButton("Send");
        chatArea.setEditable(false);
        chatArea.setFont(new Font("TextFont", Font.BOLD, 16));
        field.setFont(new Font("TextFont", Font.BOLD, 16));
        scrollPane.setPreferredSize(new Dimension(335, 475));
        field.setPreferredSize(new Dimension(335, 25));
        sendButton.setMinimumSize(new Dimension(40, 25));

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(scrollPane)
                        .addComponent(field)
                )
                .addGroup(layout.createParallelGroup()
                        .addComponent(sendButton)
                        .addComponent(usersInRooms)
                )
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(scrollPane)
                        .addComponent(usersInRooms)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(field)
                        .addComponent(sendButton)
                )
        );

        sendButton.addActionListener(this);
        field.addActionListener(this);

        setContentPane(panel);
        pack();
        System.out.println("CLIENT(ROOM): #" + roomID);
        MainFrame.yourRoomsBox.addItem("#" + roomID);
        MainFrame.yourRoomDialogs.add(this);
        usersInRooms.addItem(userName);
        if (uir!=null) {
            System.out.println(Arrays.toString(uir));
            for (String s : uir) {
                boolean alreadyAdd = false;
                for (int i = 0; i < usersInRooms.getItemCount(); i++) {
                    if (s.equals(usersInRooms.getItemAt(i))) {
                        alreadyAdd = true;
                        break;
                    }
                }
                if (!alreadyAdd) usersInRooms.addItem(s);
            }
        }
        mainFrame.update(mainFrame.getGraphics());
    }

    public int getRoomID() {
        return roomID;
    }

    public void addMessage(String message) {
        chatArea.append(message);
    }

    public void addUir(String userName) {
        usersInRooms.addItem(userName);
    }

    public void deleteUir(String userName) {
        usersInRooms.removeItem(userName);
        update(this.getGraphics());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("ACTION");
        String message = field.getText();
        if (message.length() > 0) {
            field.setText("");
            if (message.startsWith("/say")) {
                String messTo = "", toUserName = "";
                boolean flagName = true;
                char[] mess = message.toCharArray();
                for (int i = 5; i < message.length(); i++) {
                    if (mess[i] == ' ' && flagName) {
                        flagName = false;
                        continue;
                    }
                    if (flagName) toUserName += (mess[i]);
                    else messTo += (mess[i]);
                }
                System.out.println(toUserName + ": " + messTo);
                ForkJoinPool.commonPool().execute(new SendingRequest(mainFrame, ConnectingToServer.userSocket, PacketType.PRIVATE_MESSAGE, userName, toUserName, roomID, messTo));
            } else {
                ForkJoinPool.commonPool().execute(new SendingRequest(mainFrame, ConnectingToServer.userSocket, PacketType.MESSAGE, userName, roomID, message));// ОТПРАВКА СООБЩЕНИЙ
            }
            System.out.println("CLIENT: sending request for message!");
        }
    }
}
