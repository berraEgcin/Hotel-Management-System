package org.example;


import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class Receptionist extends JFrame {
    private final int receptionistId;

    public Receptionist(int receptionistId) {
        this.receptionistId = receptionistId;
        setTitle("Receptionist Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton manageScheduleButton = new JButton("Manage Housekeeping Schedule");
        JButton confirmBookingsButton = new JButton("Confirm/Deny Bookings");
        JButton viewRoomsButton = new JButton("View Available Rooms");
        JButton viewBookingsButton = new JButton("View Bookings");
        JButton processPaymentButton = new JButton("Process Payment");
        JButton assignHousekeepingButton = new JButton("Assign Housekeeping Task");
        JButton viewHousekeepersButton = new JButton("View Housekeepers Availability");
        JButton viewRequestsButton = new JButton("View Booking Requests");

        manageScheduleButton.addActionListener(e -> manageHousekeepingSchedule());
        confirmBookingsButton.addActionListener(e -> handleBookingConfirmation());
        viewRoomsButton.addActionListener(e -> viewAvailableRooms());
        viewBookingsButton.addActionListener(e -> viewAllBookings());
        processPaymentButton.addActionListener(e -> processPayment());
        assignHousekeepingButton.addActionListener(e -> assignHousekeepingTask());
        viewHousekeepersButton.addActionListener(e -> viewHousekeepersAvailability());
        viewRequestsButton.addActionListener(e -> viewBookingRequests());

        mainPanel.add(manageScheduleButton);
        mainPanel.add(confirmBookingsButton);
        mainPanel.add(viewRoomsButton);
        mainPanel.add(viewBookingsButton);
        mainPanel.add(processPaymentButton);
        mainPanel.add(assignHousekeepingButton);
        mainPanel.add(viewHousekeepersButton);
        mainPanel.add(viewRequestsButton);

        add(mainPanel);
    }


    private void manageHousekeepingSchedule() {
        // Logic to manage housekeeping schedule
        JOptionPane.showMessageDialog(this, "Manage Housekeeping Schedule functionality to be implemented.");
    }

    private void handleBookingConfirmation() {
        // Logic to confirm or deny bookings
        JOptionPane.showMessageDialog(this, "Confirm/Deny Bookings functionality to be implemented.");
    }

    private void viewAvailableRooms() {
        // Logic to view available rooms
        JOptionPane.showMessageDialog(this, "View Available Rooms functionality to be implemented.");
    }

    private void viewAllBookings() {
        // Logic to view all bookings
        JOptionPane.showMessageDialog(this, "View Bookings functionality to be implemented.");
    }

    private void processPayment() {
        // Logic to process payment
        JOptionPane.showMessageDialog(this, "Process Payment functionality to be implemented.");
    }

    private void assignHousekeepingTask() {
        // Logic to assign housekeeping tasks
        JOptionPane.showMessageDialog(this, "Assign Housekeeping Task functionality to be implemented.");
    }

    private void viewHousekeepersAvailability() {
        // Logic to view housekeepers' records and availability
        JOptionPane.showMessageDialog(this, "View Housekeepers Availability functionality to be implemented.");
    }

    private void viewBookingRequests() {
        // Logic to view booking requests
        JOptionPane.showMessageDialog(this, "View Booking Requests functionality to be implemented.");
    }
}