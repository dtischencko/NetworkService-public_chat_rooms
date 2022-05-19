package CLIENT.GUI;

import CLIENT.PROCESSING.AnswerConsumer;
import CLIENT.PROCESSING.ConnectingToServer;
import CLIENT.PROCESSING.SendingRequest;
import SERVER.RESOURCES.PacketType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;

public class MainFrame extends JFrame implements ActionListener {

    public static JComboBox<String> yourRoomsBox;
    public volatile static ArrayList<RoomDialog> yourRoomDialogs;
    private String userName;
    public static AnswerConsumer answerConsumer;
    public LoginFrame loginFrame;
    public RegisterDialog registerDialog;
    private final CyclicBarrier cyclicBarrier;
    public JComboBox<String> allRoomsInAppBox;
    public ArrayList<String> allRoomsInApp;

    public MainFrame(String title, RegisterDialog registerDialog, CyclicBarrier barrier) throws HeadlessException {
        super(title);
        this.registerDialog = registerDialog;
        this.cyclicBarrier = new CyclicBarrier(2);

        setSize(new Dimension(500, 500));
        setResizable(false);
        yourRoomDialogs = new ArrayList<>();
        allRoomsInApp = new ArrayList<>();
        JPanel panel = new JPanel();
        GroupLayout layout = new GroupLayout(panel);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        JLabel findRoomLb = new JLabel("Find room:");
        JButton findButton = new JButton("Find");
        JButton createButton = new JButton("Create");
        JButton showButton = new JButton("Show");
        JButton deleteButton = new JButton("Delete");
        findButton.setMinimumSize(new Dimension(75, 25));
        createButton.setMinimumSize(new Dimension(75, 25));
        showButton.setMinimumSize(new Dimension(75, 25));
        deleteButton.setMinimumSize(new Dimension(75, 25));
        yourRoomsBox = new JComboBox<>();
        allRoomsInAppBox = new JComboBox<>();
        JLabel roomsLb = new JLabel("Your rooms:");
        createButton.setActionCommand("CREATE_B");
        findButton.setActionCommand("FIND_B");
        showButton.setActionCommand("SHOW_B");
        deleteButton.setActionCommand("DELETE_B");
        allRoomsInAppBox.setMinimumSize(new Dimension(350, 25));

        layout.setHorizontalGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(findRoomLb)
                                .addComponent(roomsLb)
                        )
                        .addGroup(layout.createParallelGroup()
                                        .addComponent(allRoomsInAppBox)
                                        .addComponent(yourRoomsBox)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(findButton)
                                .addComponent(showButton)
                        )
                        .addGroup(layout.createParallelGroup()
                                .addComponent(createButton)
                                .addComponent(deleteButton)
                        )
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                        .addGap(20)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(findRoomLb)
                                        .addComponent(allRoomsInAppBox)
                                        .addComponent(findButton)
                                        .addComponent(createButton)
                        )
                        .addGap(25)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(roomsLb)
                                .addComponent(yourRoomsBox)
                                .addComponent(showButton)
                                .addComponent(deleteButton)
                        )
                        .addGap(25)
        );

        findButton.addActionListener(this);
        createButton.addActionListener(this);
        showButton.addActionListener(this);
        deleteButton.addActionListener(this);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ForkJoinPool.commonPool().invoke(new SendingRequest(MainFrame.this, ConnectingToServer.userSocket, PacketType.EXIT, userName));
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException ex) {
                    ex.printStackTrace();
                }
                System.exit(0);
            }
        });

        panel.setLayout(layout);
        setContentPane(panel);
        pack();

        ForkJoinPool.commonPool().execute(answerConsumer = new AnswerConsumer(this, ConnectingToServer.userSocket, yourRoomDialogs, cyclicBarrier, barrier));
    }

    public static AnswerConsumer getAnswerConsumer() {
        return answerConsumer;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "FIND_B" -> {
                if (answerConsumer.getUserName() == null) {
                    answerConsumer.setUserName(userName);
                }
                if (allRoomsInAppBox.getItemCount() > 0 && allRoomsInAppBox.getSelectedItem() != null) {
                    String roomStr = (String) allRoomsInAppBox.getSelectedItem();
                    System.out.println(allRoomsInAppBox.getSelectedIndex());
                    assert roomStr != null;
                    int roomID = Integer.parseInt(roomStr.substring(1));
                    ForkJoinPool.commonPool().execute(new SendingRequest(this, ConnectingToServer.userSocket, PacketType.ROOM_CONNECT, userName, roomID)); // TODO: ИЗМЕНИТЬ КОМНАТУ ПОСЛЕ ТЕСТОВ
                    System.out.println("CLIENT: start sending request");
                }
            }
            case "CREATE_B" -> {
                if (answerConsumer.getUserName() == null) {
                    answerConsumer.setUserName(userName);
                }
                ForkJoinPool.commonPool().execute(new SendingRequest(this, ConnectingToServer.userSocket, PacketType.ROOM_CREATE, userName)); // Создать новую комнату
                System.out.println("CLIENT: start sending request");
            }
            case "SHOW_B" -> {
                if (yourRoomsBox.getSelectedIndex() != -1) {
                    String rID = (String) yourRoomsBox.getSelectedItem();
                    for (RoomDialog r :
                            yourRoomDialogs) {
                        if (("#" + r.getRoomID()).equals(rID)) {
                            r.setVisible(true);
                            break;
                        }
                    }
                }
            }
            case "DELETE_B" -> {
                if (yourRoomsBox.getSelectedIndex() != -1) {
                    String rID = (String) yourRoomsBox.getSelectedItem();
                    Object selectedOb = yourRoomsBox.getSelectedItem();

                    for (RoomDialog r :
                            yourRoomDialogs) {
                        if (("#" + r.getRoomID()).equals(rID)) {
                            ForkJoinPool.commonPool().execute(new SendingRequest(this, ConnectingToServer.userSocket, PacketType.ROOM_DELETE, userName, r.getRoomID()));
                            r.dispose();
                            yourRoomDialogs.remove(r);
                            yourRoomsBox.removeItem(selectedOb);
                            break;
                        }
                    }
                }
            }
        }
    }
}