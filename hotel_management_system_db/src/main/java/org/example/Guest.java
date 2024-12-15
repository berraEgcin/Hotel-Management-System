package org.example;

import javax.swing.*;
import java.awt.*;

public class Guest extends JFrame {
    private final int guestId;

    public Guest(int guestId) {
        this.guestId = guestId;
        setTitle("Guest Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(5, 1, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton addNewBookingButton = new JButton("Add New Booking");
        JButton viewAvailableRoomsButton = new JButton("View Available Rooms");
        JButton viewMyBookingsButton = new JButton("View My Bookings");
        JButton cancelBookingButton = new JButton("Cancel Booking");

        addNewBookingButton.addActionListener(e -> addNewBooking());
        viewAvailableRoomsButton.addActionListener(e -> viewAvailableRooms());
        viewMyBookingsButton.addActionListener(e -> viewMyBookings());
        cancelBookingButton.addActionListener(e -> cancelBooking());

        mainPanel.add(addNewBookingButton);
        mainPanel.add(viewAvailableRoomsButton);
        mainPanel.add(viewMyBookingsButton);
        mainPanel.add(cancelBookingButton);

        add(mainPanel);
    }

    private void addNewBooking() {
        // Prompt the user for the number of people and duration
        String peopleInput = JOptionPane.showInputDialog("Enter number of people:");
        String daysInput = JOptionPane.showInputDialog("Enter number of days:");

        try {
            int people = Integer.parseInt(peopleInput);
            int days = Integer.parseInt(daysInput);

            if (people > 0 && days > 0) {
                // Logic to add a new booking for the specified number of people and days
                JOptionPane.showMessageDialog(this, "Booking added for " + people + " people for " + days + " days.");
            } else {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for people and days.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter numbers only.");
        }
    }

    private void viewAvailableRooms() {
        // Logic to display available rooms
        JOptionPane.showMessageDialog(this, "Displaying available rooms.");
    }

    private void viewMyBookings() {
        // Logic to display the current guest's bookings
        JOptionPane.showMessageDialog(this, "Displaying your bookings.");
    }

    private void cancelBooking() {
        // Logic to cancel an existing booking
        String bookingIdInput = JOptionPane.showInputDialog("Enter the booking ID to cancel:");

        try {
            int bookingId = Integer.parseInt(bookingIdInput);
            // Logic to cancel the booking
            JOptionPane.showMessageDialog(this, "Booking with ID " + bookingId + " has been cancelled.");
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid booking ID. Please enter a valid number.");
        }
    }
}
