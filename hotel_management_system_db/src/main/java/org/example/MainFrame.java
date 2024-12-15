package org.example;


import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Hotel Management System");
        setSize(600, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        LoginPanel loginPanel = new LoginPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Login", loginPanel);

        add(tabbedPane);

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame();
            }
        });
    }
}


