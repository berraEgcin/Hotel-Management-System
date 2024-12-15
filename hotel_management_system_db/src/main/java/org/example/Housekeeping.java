package org.example;

import javax.swing.*;
import java.awt.*;

public class Housekeeping extends JFrame {
    private final int employeeId;

    public Housekeeping(int employeeId) {
        this.employeeId = employeeId;
        setTitle("Housekeeping Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton viewRoomAvailabilityButton = new JButton("View Room Availability");
        JButton viewPendingTasksButton = new JButton("View Pending Housekeeping Tasks");
        JButton viewCompletedTasksButton = new JButton("View Completed Housekeeping Tasks");
        JButton updateTaskStatusButton = new JButton("Update Task Status to Completed");
        JButton viewMyScheduleButton = new JButton("View My Cleaning Schedule");

        viewRoomAvailabilityButton.addActionListener(e -> viewRoomAvailability());
        viewPendingTasksButton.addActionListener(e -> viewPendingTasks());
        viewCompletedTasksButton.addActionListener(e -> viewCompletedTasks());
        updateTaskStatusButton.addActionListener(e -> updateTaskStatus());
        viewMyScheduleButton.addActionListener(e -> viewCleaningSchedule());

        mainPanel.add(viewRoomAvailabilityButton);
        mainPanel.add(viewPendingTasksButton);
        mainPanel.add(viewCompletedTasksButton);
        mainPanel.add(updateTaskStatusButton);
        mainPanel.add(viewMyScheduleButton);

        add(mainPanel);
    }

    private void viewRoomAvailability() {
        // Logic to display room availability
    }

    private void viewPendingTasks() {
        // Logic to display pending housekeeping tasks
    }

    private void viewCompletedTasks() {
        // Logic to display completed housekeeping tasks
    }

    private void updateTaskStatus() {
        // Logic to update task status to completed
    }

    private void viewCleaningSchedule() {
        // Logic to display the cleaning schedule for the current employee
    }
}
