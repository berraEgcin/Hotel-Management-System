package org.example;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPanel extends JPanel {
    private JTextField idField;
    private JComboBox<String> roleComboBox;
    private JButton submitButton;

    public LoginPanel() {
        setLayout(new BorderLayout());
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 5, 5));

        idField = new JTextField(20);
        roleComboBox = new JComboBox<>(new String[]{"Guest", "Admin", "Receptionist", "Housekeeping"});
        submitButton = new JButton("Submit");

        loginPanel.add(new JLabel("Select Role:"));
        loginPanel.add(roleComboBox);
        loginPanel.add(new JLabel("ID:"));
        loginPanel.add(idField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(submitButton);

        add(loginPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> {
            String selectedRole = (String) roleComboBox.getSelectedItem();
            String idText = idField.getText();

            if (idExistsInDatabase(selectedRole, idText)) {
                openNewWindow(selectedRole, idText);
            } else {
                JOptionPane.showMessageDialog(LoginPanel.this, "ID does not exist.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private boolean idExistsInDatabase(String role, String id) {
        String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
        String user = "root";
        String password = "";

        String query = "";

        switch (role) {
            case "Guest":
                query = "SELECT COUNT(*) FROM guest WHERE guest_ID = ?";
                break;
            case "Admin":
                query = "SELECT COUNT(*) FROM administrator WHERE admin_ID = ?";
                break;
            case "Receptionist":
                query = "SELECT COUNT(*) FROM receptionist WHERE employee_ID = ?";
                break;
            case "Housekeeping":
                query = "SELECT COUNT(*) FROM housekeeping WHERE employee_ID = ?";
                break;
            default:
                JOptionPane.showMessageDialog(this, "Invalid role selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return false;
        }

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, Integer.parseInt(id));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            //e.printStackTrace();
        }
        return false;
    }

    private void openNewWindow(String role, String id) {
        int idN = Integer.parseInt(id);

        switch (role) {
            case "Admin":
                JFrame newAdmin = new Admin(idN);
                newAdmin.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                newAdmin.setSize(400, 300);
                newAdmin.setVisible(true);
                break;
            case "Receptionist":
                JFrame newRecep = new Receptionist(idN);
                newRecep.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                newRecep.setSize(400, 300);
                newRecep.setVisible(true);
                break;
            case "Housekeeping":
                JFrame newH = new Housekeeping(idN);
                newH.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                newH.setSize(400, 300);
                newH.setVisible(true);
                break;
            case "Guest":
                JFrame newG = new Guest(idN);
                newG.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                newG.setSize(400, 300);
                newG.setVisible(true);
                break;
        }
    }
}
