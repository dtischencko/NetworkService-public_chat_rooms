package CLIENT.GUI;

import CLIENT.PROCESSING.ConnectingToServer;
import CLIENT.PROCESSING.SendingRequest;
import SERVER.RESOURCES.PacketType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ForkJoinPool;

public class RegisterDialog extends JDialog implements ActionListener {

    private JTextField textLog;
    private JPasswordField textPas;
    private JPasswordField textConfPas;
    private MainFrame mainFrame;

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public RegisterDialog(Frame owner, String title, boolean modal) {
        super(owner, title, modal);

        Dimension monSize = Toolkit.getDefaultToolkit().getScreenSize();
        JPanel panel = new JPanel();
        JButton regButton = new JButton("Register");
        textLog = new JTextField();
        textPas = new JPasswordField();
        textConfPas = new JPasswordField();
        JLabel labelLog = new JLabel("Your user name:");
        JLabel labelPas = new JLabel("Your password:");
        JLabel labelConfPas = new JLabel("Confirm password:");

        GroupLayout regLayout = new GroupLayout(panel);
        panel.setLayout(regLayout);
        textLog.setMinimumSize(new Dimension(175,25));
        textLog.setMaximumSize(new Dimension(175,25));
        textPas.setMinimumSize(new Dimension(175,25));
        textPas.setMaximumSize(new Dimension(175,25));
        textConfPas.setMinimumSize(new Dimension(175,25));
        textConfPas.setMaximumSize(new Dimension(175,25));
        textLog.setFont(new Font("TextFont", Font.BOLD, 16));
        textPas.setFont(new Font("TextFont", Font.BOLD, 16));
        textConfPas.setFont(new Font("TextFont", Font.BOLD, 16));
        labelLog.setFont(new Font("TextFont", Font.BOLD, 14));
        labelPas.setFont(new Font("TextFont", Font.BOLD, 14));
        labelConfPas.setFont(new Font("TextFont", Font.BOLD, 14));
        regButton.setMinimumSize(new Dimension(80,25));
        regLayout.setAutoCreateGaps(true);
        regLayout.setAutoCreateContainerGaps(true);

        regLayout.setHorizontalGroup(regLayout.createSequentialGroup()
                .addGroup(regLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(labelLog)
                        .addComponent(labelPas)
                        .addComponent(labelConfPas)
                )
                .addGroup(regLayout.createParallelGroup()
                        .addComponent(textLog)
                        .addComponent(textPas)
                        .addComponent(textConfPas)
                )
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(regButton)
        );
        regLayout.setVerticalGroup(regLayout.createSequentialGroup()
                .addGroup(regLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelLog)
                        .addComponent(textLog)
                )
                .addGroup(regLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelPas)
                        .addComponent(textPas)
                )
                .addGroup(regLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(labelConfPas)
                        .addComponent(textConfPas)
                )
                .addGap(40)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(regLayout.createParallelGroup()
                        .addComponent(regButton)
                )
        );

        setContentPane(panel);

        regButton.addActionListener(this);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds((monSize.width/2), (monSize.height/2), 300, 400); //300 400
        setResizable(false);
        pack();
        setLocationByPlatform(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String userName;
        char[] passwordC, confPassC;
        userName = textLog.getText();
        passwordC = textPas.getPassword();
        confPassC = textConfPas.getPassword();
        String password = "", confPass = "";
        for (int i = 0; i < passwordC.length; i++) {
            password += passwordC[i];
            confPass += confPassC[i];
        }
        if (userName.length() > 0 && password.length() > 0 && confPass.length() > 0 && password.equals(confPass)) {
            mainFrame.setUserName(userName);
            mainFrame.loginFrame.setUserNameAC(userName);
            ForkJoinPool.commonPool().invoke(new SendingRequest(mainFrame, ConnectingToServer.userSocket, PacketType.REGISTRATION, userName, password));
        }
    }
}
