package CLIENT.START;

import javax.swing.*;

import CLIENT.GUI.IPRequest;
import CLIENT.GUI.LoginFrame;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Main {
    public static void main(String[] args) {
        CyclicBarrier barrier = new CyclicBarrier(2);
        new IPRequest(barrier);
        try {
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new LoginFrame("Welcome to PCR"));
    }
}
