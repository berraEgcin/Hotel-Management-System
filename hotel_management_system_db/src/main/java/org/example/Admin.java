package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class Admin extends JFrame {
    private final int adminId;

    public Admin(int adminId) {
        this.adminId = adminId;
        setTitle("Admin Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton addRoomButton = new JButton("Add Room");
        JButton deleteRoomButton = new JButton("Delete Room");
        JButton manageRoomButton = new JButton("Manage Room Status");
        JButton addUserButton = new JButton("Add User Account");
        JButton viewUsersButton = new JButton("View User Accounts");
        JButton revenueReportButton = new JButton("Generate Revenue Report");
        JButton viewBookingsButton = new JButton("View All Booking Records");
        JButton viewHousekeepingButton = new JButton("View All Housekeeping Records");
        JButton viewPopularRoomsButton = new JButton("View Most Booked Room Types");
        JButton viewEmployeesButton = new JButton("View All Employees");

        addRoomButton.addActionListener(e -> showAddRoomDialog());
        deleteRoomButton.addActionListener(e -> showDeleteRoomDialog());
        manageRoomButton.addActionListener(e -> showManageRoomDialog());
        addUserButton.addActionListener(e -> showAddUserDialog());
        viewUsersButton.addActionListener(e -> viewUserAccounts());
        revenueReportButton.addActionListener(e -> generateRevenueReport());
        viewBookingsButton.addActionListener(e -> viewAllBookings());
        viewHousekeepingButton.addActionListener(e -> viewHousekeepingRecords());
        viewPopularRoomsButton.addActionListener(e -> viewPopularRoomTypes());
        viewEmployeesButton.addActionListener(e -> viewEmployees());

        mainPanel.add(addRoomButton);
        mainPanel.add(deleteRoomButton);
        mainPanel.add(manageRoomButton);
        mainPanel.add(addUserButton);
        mainPanel.add(viewUsersButton);
        mainPanel.add(revenueReportButton);
        mainPanel.add(viewBookingsButton);
        mainPanel.add(viewHousekeepingButton);
        mainPanel.add(viewPopularRoomsButton);
        mainPanel.add(viewEmployeesButton);

        add(mainPanel);
    }
    private void showAddRoomDialog() {
        JTextField roomIdField = new JTextField();
        JTextField typeIdField = new JTextField();
        JComboBox<String> cleanlinessComboBox = new JComboBox<>(new String[]{"Clean", "Dirty", "In Progress"});
        JCheckBox availabilityCheckBox = new JCheckBox("Available");

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Room ID:"));
        panel.add(roomIdField);
        panel.add(new JLabel("Type ID:"));
        panel.add(typeIdField);
        panel.add(new JLabel("Cleanliness:"));
        panel.add(cleanlinessComboBox);
        panel.add(new JLabel("Available:"));
        panel.add(availabilityCheckBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Room", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int roomId = Integer.parseInt(roomIdField.getText());
            int typeId = Integer.parseInt(typeIdField.getText());
            String cleanliness = (String) cleanlinessComboBox.getSelectedItem();
            boolean isAvailable = availabilityCheckBox.isSelected();

            addRoomToDatabase(roomId, typeId, cleanliness, isAvailable);
        }
    }

    private void addRoomToDatabase(int roomId, int typeId, String isClean, boolean isAvailable) {
        String query = "{CALL admin_add_room(?, ?, ?, ?)}";
        String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
        String username = "root";
        String password = "NZHBCt5*";

        try (Connection conn =DriverManager.getConnection(url, username, password);
             CallableStatement stmt = conn.prepareCall(query)) {

            stmt.setInt(1, roomId);
            stmt.setInt(2, typeId);
            stmt.setString(3, isClean);
            stmt.setBoolean(4, isAvailable);

            stmt.execute();
            JOptionPane.showMessageDialog(this, "Room added successfully!");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding room: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Room ID and Type ID must be valid numbers.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDeleteRoomDialog() {
        // Logic to show dialog for deleting a room
    }

    private void showManageRoomDialog() {
        // Logic to show dialog for managing room status
    }

    private void showAddUserDialog() {
        // Logic to show dialog for adding a user account
    }

    private void viewUserAccounts() {
        // Logic to view user accounts
    }

    private void generateRevenueReport() {
        // Logic to generate revenue report
    }

    private void viewAllBookings() {
        // Logic to view all booking records
    }

    private void viewHousekeepingRecords() {
        // Logic to view all housekeeping records
    }

    private void viewPopularRoomTypes() {
        // Logic to view most booked room types
    }

    private void viewEmployees() {
        // Logic to view all employees with their roles
    }
}