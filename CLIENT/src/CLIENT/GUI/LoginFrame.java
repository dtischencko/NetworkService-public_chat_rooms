package CLIENT.GUI;

import CLIENT.PROCESSING.ConnectingToServer;
import CLIENT.PROCESSING.SendingRequest;
import SERVER.RESOURCES.PacketType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;

public class LoginFrame extends JFrame implements ActionListener {

    private MainFrame mainFrame = null;
    private RegisterDialog registerDialog = null;
    private String userName;
    private String password = "";
    private JTextField textLog;
    private JPasswordField textPas;
    public final CyclicBarrier barrier;

    public LoginFrame(String title) throws HeadlessException {
        super(title);
        barrier = new CyclicBarrier(2);

        Dimension monSize = Toolkit.getDefaultToolkit().getScreenSize();
        JPanel panel = new JPanel();
        JButton logButton = new JButton("Log in");
        JButton regButton = new JButton("Register");
        textLog = new JTextField();
        textPas = new JPasswordField();
        JLabel labelLog = new JLabel("User name:");
        JLabel labelPas = new JLabel("Password:");

        GroupLayout layout = new GroupLayout(panel);
        panel.setLayout(layout);
        textLog.setMinimumSize(new Dimension(175,25));
        textLog.setMaximumSize(new Dimension(175,25));
        textPas.setMinimumSize(new Dimension(175,25));
        textPas.setMaximumSize(new Dimension(175,25));
        textLog.setFont(new Font("TextFont", Font.BOLD, 16));
        textPas.setFont(new Font("TextFont", Font.BOLD, 16));
        labelLog.setFont(new Font("TextFont", Font.BOLD, 14));
        labelPas.setFont(new Font("TextFont", Font.BOLD, 14));
        logButton.setMinimumSize(new Dimension(60,25));
        regButton.setMinimumSize(new Dimension(80,25));
        logButton.setActionCommand("LOG");
        regButton.setActionCommand("REG");
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(labelLog)
                        .addComponent(labelPas)
                )
                .addGroup(layout.createParallelGroup()
                        .addComponent(textLog)
                        .addComponent(textPas)
                )
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(logButton)
                .addComponent(regButton)
        );
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelLog)
                        .addComponent(textLog)
                )
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelPas)
                        .addComponent(textPas)
                )
                .addGap(40)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(logButton)
                                .addComponent(regButton)
                        )
        );
        setContentPane(panel);

        logButton.addActionListener(this);
        regButton.addActionListener(this);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBounds((monSize.width/2), (monSize.height/2), 300, 400); //300 400
        setResizable(false);
        pack();
        setLocationByPlatform(true);

        mainFrame = new MainFrame("PublicChatRooms", registerDialog = new RegisterDialog(this, "Registration", true), barrier);
        mainFrame.loginFrame = this;
        registerDialog.setMainFrame(mainFrame);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        switch (cmd) {
            case "LOG" -> {
                userName = textLog.getText();
                char[] passwordC = textPas.getPassword();
                for (char c : passwordC) password += c;
                if (userName.length()>0 && password.length()>0) {
                    mainFrame.setUserName(userName);
                    this.setUserNameAC(userName);
                    ForkJoinPool.commonPool().invoke(new SendingRequest(mainFrame, ConnectingToServer.userSocket, PacketType.LOGIN, userName, password));

                }
            }
            case "REG" -> {
                System.out.println("REG");
                registerDialog.setVisible(true);
            }
        }
    }

    public void setUserNameAC(String userName) {
        MainFrame.getAnswerConsumer().setUserName(userName);
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
}
