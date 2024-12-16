import javax.swing.*;
import java.awt.*;
import java.sql.*;

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
        String roomIdInput = JOptionPane.showInputDialog("Enter Room ID you are interested in:");
        String peopleInput = JOptionPane.showInputDialog("Enter number of people:");
        String checkInDateInput = JOptionPane.showInputDialog("Enter Check-In Date (YYYY-MM-DD):");
        String checkOutDateInput = JOptionPane.showInputDialog("Enter Check-Out Date (YYYY-MM-DD):");

        try {
            int roomId = Integer.parseInt(roomIdInput);
            int people = Integer.parseInt(peopleInput);

            Date checkInDate = Date.valueOf(checkInDateInput);
            Date checkOutDate = Date.valueOf(checkOutDateInput);

            if (people > 0 && checkInDate.before(checkOutDate)) {
                String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
                String username = "root";
                String password = "";

                String checkRoomQuery = "SELECT * FROM room WHERE room_ID = ? AND isAvailable = true AND isClean = 'Clean'";

                try (Connection conn = DriverManager.getConnection(url, username, password);
                     PreparedStatement checkStmt = conn.prepareStatement(checkRoomQuery)) {

                    checkStmt.setInt(1, roomId);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        String insertBookingQuery = "INSERT INTO booking (guest_ID, room_ID, num_Of_Guests, check_in, check_out) VALUES (?, ?, ?, ?, ?)";

                        try (PreparedStatement insertStmt = conn.prepareStatement(insertBookingQuery)) {
                            insertStmt.setInt(1, guestId);
                            insertStmt.setInt(2, roomId);
                            insertStmt.setInt(3, people);
                            insertStmt.setDate(4, checkInDate);
                            insertStmt.setDate(5, checkOutDate);

                            insertStmt.executeUpdate();
                            JOptionPane.showMessageDialog(this, "Booking added for Room ID " + roomId + " for " + people + " people from " + checkInDate + " to " + checkOutDate + ".");
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "The room is either not available or not clean. Please choose another room.");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please enter valid numbers for people and ensure check-out date is after check-in date.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter numbers only.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD format.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewAvailableRooms() {
        String query = "SELECT * FROM room WHERE isAvailable = true";
        String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
        String username = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            StringBuilder result = new StringBuilder("Available Rooms:\n");

            while (rs.next()) {
                int roomId = rs.getInt("room_ID");
                String type = rs.getString("type_ID");
                String cleanliness = rs.getString("isClean");
                result.append("Room ID: ").append(roomId)
                        .append(", Type: ").append(type)
                        .append(", Cleanliness: ").append(cleanliness)
                        .append("\n");
            }

            if (result.toString().equals("Available Rooms:\n")) {
                result.append("No rooms are available at the moment.");
            }

            JOptionPane.showMessageDialog(this, result.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching available rooms: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewMyBookings() {
        String query = "SELECT * FROM booking WHERE guest_ID = ?";
        String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
        String username = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, guestId);
            ResultSet rs = stmt.executeQuery();

            StringBuilder result = new StringBuilder("Your Bookings:\n");

            while (rs.next()) {
                int bookingId = rs.getInt("booking_ID");
                int roomId = rs.getInt("room_ID");
                Date startDate = rs.getDate("check_in");
                Date endDate = rs.getDate("check_out");
                result.append("Booking ID: ").append(bookingId)
                        .append(", Room ID: ").append(roomId)
                        .append(", Start Date: ").append(startDate)
                        .append(", End Date: ").append(endDate)
                        .append("\n");
            }

            if (result.toString().equals("Your Bookings:\n")) {
                result.append("You have no bookings.");
            }

            JOptionPane.showMessageDialog(this, result.toString());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching your bookings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelBooking() {
        String bookingIdInput = JOptionPane.showInputDialog("Enter the booking ID to cancel:");

        try {
            int bookingId = Integer.parseInt(bookingIdInput);

            String query = "DELETE FROM booking WHERE booking_ID = ? AND guest_ID = ?";
            String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
            String username = "root";
            String password = "";

            try (Connection conn = DriverManager.getConnection(url, username, password);
                 PreparedStatement stmt = conn.prepareStatement(query)) {

                stmt.setInt(1, bookingId);
                stmt.setInt(2, guestId);

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Booking with ID " + bookingId + " has been cancelled.");
                } else {
                    JOptionPane.showMessageDialog(this, "Booking ID not found or does not belong to you.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error cancelling booking: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid booking ID. Please enter a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
