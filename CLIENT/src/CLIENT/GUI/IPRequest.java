package CLIENT.GUI;

import CLIENT.PROCESSING.ConnectingToServer;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ForkJoinPool;

public class IPRequest extends JFrame {
    public IPRequest(CyclicBarrier barrier) throws HeadlessException {

        setSize(new Dimension(200,100));
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JTextField textIP = new JTextField();
        textIP.setFont(new Font("TextFont", Font.BOLD, 16));
        textIP.setText("SERVER_IP");
        add(textIP);

        textIP.addActionListener((actionEvent) -> {
            String IP = textIP.getText().trim();
            if (IP.length() > 0) {
                ForkJoinPool.commonPool().invoke(new ConnectingToServer(IP));
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                dispose();
            }
        });

        setVisible(true);
    }
}
