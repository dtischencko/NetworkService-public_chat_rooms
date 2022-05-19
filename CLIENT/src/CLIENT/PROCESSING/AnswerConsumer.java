package CLIENT.PROCESSING;

import CLIENT.GUI.MainFrame;
import CLIENT.GUI.RoomDialog;
import SERVER.RESOURCES.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.RecursiveAction;

public class AnswerConsumer extends RecursiveAction {

    public static volatile ArrayList<RoomDialog> roomDialogs;
    private ObjectInputStream inOb;
    private MainFrame mainFrame;
    private Socket userSocket;
    private int roomID;
    private String userName = null;
    private boolean isConnected = true;
    private final CyclicBarrier cyclicBarrier;
    private final CyclicBarrier barrier;


    public AnswerConsumer(MainFrame mainFrame, Socket userSocket, ArrayList<RoomDialog> yourRoomDialogs, CyclicBarrier cyclicBarrier, CyclicBarrier barrier) {
        roomDialogs = yourRoomDialogs;
        this.mainFrame = mainFrame;
        this.userSocket = userSocket;
        this.inOb = ConnectingToServer.inOb;
        this.cyclicBarrier = cyclicBarrier;
        this.barrier = barrier;
        System.out.println("====CLIENT ANSWER CONSUMER HAS STARTED====");
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    protected void compute() {
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        do {
            try {
                System.out.println("CLIENT " + userName + " waiting input data...");
                Packet answerPacket = (Packet) inOb.readObject();
                switch (answerPacket.getPacketType()) {
                    case MESSAGE -> {
                        for (RoomDialog r:
                             roomDialogs) {
                            if (r.getRoomID() == answerPacket.getRoomID()) {
                                System.out.println("CLIENT(MESSAGE): message!!");
                                if (Objects.equals(answerPacket.getUserName(), userName)) {
                                    r.addMessage("<You> " + answerPacket.getMessage() + "\n");
                                } else {
                                    r.addMessage("<" + answerPacket.getUserName() + "> " + answerPacket.getMessage() + "\n");
                                }
                                break;
                            }
                        }
                    }
                    case PRIVATE_MESSAGE -> {
                        for (RoomDialog r:
                             roomDialogs) {
                            if (r.getRoomID() == answerPacket.getRoomID()) {
                                System.out.println("CLIENT(PRIVATE_MESSAGE): message!!");
                                if (Objects.equals(answerPacket.getUserName(), userName)) {
                                    r.addMessage("<You>>" + answerPacket.getToUserName() + ">" + answerPacket.getMessage() + "\n");
                                } else {
                                    r.addMessage("<" + answerPacket.getUserName() + ">>" + answerPacket.getToUserName() + "> " + answerPacket.getMessage() + "\n");
                                }
                                break;
                            }
                        }
                    }
                    case LOGIN -> {
                        if (answerPacket.isSuccess()) {
                            mainFrame.setUserName(answerPacket.getUserName());
                            mainFrame.setVisible(true);
                            mainFrame.loginFrame.dispose();
                            System.out.println("LOG as " + answerPacket.getUserName());

                            if (!answerPacket.isDbEmpty()) {
                                Map<String, ArrayList<Integer>> roomsInfo = new HashMap<>();
                                System.out.println("CLIENT: waiting data from db");
                                Object roomsInfoOb = inOb.readObject();
                                roomsInfo = (Map<String, ArrayList<Integer>>) roomsInfoOb;
                                System.out.println("CLIENT: data rooms is here " + userName);
                                Map<Integer, ArrayList<String>> usersInfo = new HashMap<>();
                                System.out.println("CLIENT: waiting data from db");
                                Object usersInfoOb = inOb.readObject();
                                usersInfo = (Map<Integer, ArrayList<String>>) usersInfoOb;
                                System.out.println("CLIENT: data users is here " + userName);
                                for (Integer rid:
                                        roomsInfo.get(userName)) {
                                    int posRid = (rid < 0) ? rid*(-1) : rid;
                                    String title = "Room #" + posRid;
                                    RoomDialog roomDialog = new RoomDialog(mainFrame, title, false, posRid, userName, null);
                                    System.out.println(usersInfo.get(rid));
                                    for (String name:
                                            usersInfo.get(posRid)) {
                                        System.out.println("CLIENT: WRITE USER " + name);
                                        if (name.equals(userName)) continue;
                                        roomDialog.addUir(name);
                                    }
                                    roomDialogs.add(roomDialog);
                                    roomDialog.update(roomDialog.getGraphics());
                                }
                                for (ArrayList<Integer> arrayList:
                                     roomsInfo.values()) {
                                    for (Integer rid:
                                         arrayList) {
                                        int posRid = (rid < 0) ? rid*(-1) : rid;
                                        if (!mainFrame.allRoomsInApp.contains("#" + posRid) && posRid != 0) {
                                            mainFrame.allRoomsInApp.add("#" + posRid);
                                        }
                                    }
                                }
                                boolean canAdd = true;
                                for (String s:
                                     mainFrame.allRoomsInApp) {
                                    for (RoomDialog r:
                                         roomDialogs) {
                                        if (("#" + r.getRoomID()).equals(s)) {
                                            System.out.println("HERE"); //TODO: HERE
                                            canAdd = false;
                                            break;
                                        }
                                        System.out.println(("#" + r.getRoomID()) + " s: " + (s) + " canAdd: " + canAdd);
                                    }
                                    if (canAdd) mainFrame.allRoomsInAppBox.addItem(s);
                                    canAdd = true;
                                }
                                System.out.println("ALL ROOMS IN APP IN LOGIN TIME:\n" + mainFrame.allRoomsInApp);
                                mainFrame.update(mainFrame.getGraphics());
                            }
                        } else System.out.println("CLIENT CAN NOT LOG IN");
                    }
                    case REGISTRATION -> {
                        if (answerPacket.isSuccess()) {
                            mainFrame.setUserName(answerPacket.getUserName());
                            mainFrame.setVisible(true);
                            mainFrame.registerDialog.dispose();
                            mainFrame.loginFrame.dispose();
                            System.out.println("REG + LOG as " + answerPacket.getUserName());
                        } else System.out.println("CLIENT CAN NOT REG");
                    }
                    case ROOM_CREATE -> {
                        System.out.println("CLIENT(ROOM_CREATE): answer here");
                        String title = "Room #" + answerPacket.getRoomID();
                        RoomDialog roomDialog = new RoomDialog(mainFrame, title, false, answerPacket.getRoomID(), answerPacket.getUserName(), null);
                        roomDialogs.add(roomDialog);
                        roomDialog.setVisible(true);
                    }
                    case ROOM_CONNECT -> {
                        System.out.println("CLIENT(ROOM_CONNECT): answer here");
                        switch (answerPacket.getMessage()) {
                            case "CANCONNECT" -> {
                                String title = "Room #" + answerPacket.getRoomID();
                                RoomDialog roomDialog = new RoomDialog(mainFrame, title, false, answerPacket.getRoomID(), answerPacket.getUserName(), answerPacket.getUir());
                                roomDialogs.add(roomDialog);
                                roomDialog.setVisible(true);
                            }
                            case "USERS" -> {
                                System.out.println("CLIENT(USERS)");
                                for (RoomDialog r:
                                     roomDialogs) {
                                    if (r.getRoomID() == answerPacket.getRoomID()) {
                                        r.addUir(answerPacket.getUserName());
                                        r.update(r.getGraphics());
                                        break;
                                    }
                                }
                            }
                            case "CANTCONNECT" -> {
                                // TODO: ОБРАБОТКА НА КЛИЕНТЕ НЕЛЬЗЯ ПОДКЛЮЧИТЬСЯ
                                System.out.println("CLIENT(ROOM_CONNECT):cant connect incorrect answer");
                            }
                            default -> {
                                System.out.println("CLIENT(ROOM_CONNECT): incorrect answer");
                            }
                        }
                    }
                    case ROOM_UPDATE -> {
                        boolean canAdd = true;
                        System.out.println("CLIENT(ROOM_UPDATE)");
                        if (answerPacket.isCreate()) {
                            for (RoomDialog r:
                                    roomDialogs) {
                                if (!("#" + r.getRoomID()).equals("#" + answerPacket.getRoomID())) {
                                    canAdd = false;
                                    break;
                                }
                            }
                            if (canAdd) mainFrame.allRoomsInAppBox.addItem("#" + answerPacket.getRoomID());
                        }
                        else if (!answerPacket.isCreate()) {
                            mainFrame.allRoomsInAppBox.removeItem("#" + answerPacket.getRoomID());
                        }
                        mainFrame.update(mainFrame.getGraphics());
                    }
                    case ROOM_DELETE -> {
                        if (answerPacket.getMessage().equals("DUIR")) {
                            for (RoomDialog r:
                                 roomDialogs) {
                                if (r.getRoomID() == answerPacket.getRoomID()) {
                                    r.deleteUir(answerPacket.getUserName());
                                    mainFrame.update(mainFrame.getGraphics());
                                    break;
                                }
                            }
                        } else {
                            System.out.println("CLIENT(ROOM_DELETE)");
                            for (RoomDialog r :
                                    MainFrame.yourRoomDialogs) {
                                if (r.getRoomID() == answerPacket.getRoomID()) r.dispose();
                            }
                            System.out.println("CLIENT: DELETE room from client");
                            MainFrame.yourRoomDialogs.removeIf(r -> r.getRoomID() == answerPacket.getRoomID());
                            for (int i = 0; i < MainFrame.yourRoomsBox.getItemCount(); i++) {
                                if (Objects.equals(MainFrame.yourRoomsBox.getItemAt(i), "#" + answerPacket.getRoomID())) {
                                    MainFrame.yourRoomsBox.removeItem(MainFrame.yourRoomsBox.getItemAt(i));
                                }
                            }
                            mainFrame.update(mainFrame.getGraphics());
                        }
                    }
                    case EXIT -> {
                        System.out.println("CLIENT EXIT...");
                        isConnected = false;
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
            if (!isConnected) {
                try {
                    cyclicBarrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
            }
        } while (isConnected);
    }
}
