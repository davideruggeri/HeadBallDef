package it.unibs.pajc.game;

import javax.swing.*;
import java.awt.*;

public class HeadBallApp {
    private JFrame frame;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    HeadBallApp window = new HeadBallApp();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();}}});
    }

    public HeadBallApp() {
        initialize();}

    private void initialize() {
        frame = new JFrame();
        frame.setBounds(300, 150, 1000, 600);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Background sv = new Background();
        frame.getContentPane().add(sv, BorderLayout.CENTER);

    }
}
