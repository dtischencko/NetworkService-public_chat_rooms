package SERVER.START;

import SERVER.PROCESSING.RequestHandler;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ForkJoinPool;

public class Main extends JFrame {

    public Main(String title) throws HeadlessException {
        super(title);
        setSize(new Dimension(425,100));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        InetAddress address = null;
        JTextField textField = new JTextField();
        textField.setMinimumSize(new Dimension(300, 25));
        textField.setFont(new Font("TextFont", Font.BOLD, 24));
        textField.setEditable(false);
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        assert address != null;
        textField.setText(address.toString());
        add(textField);

        setVisible(true);
    }

    public static void main(String[] args) {

        try {
            ForkJoinPool.commonPool().execute(new RequestHandler());
        } catch (IOException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new Main("SERVER"));
    }
}
