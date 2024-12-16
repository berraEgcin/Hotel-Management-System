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
        String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
        String username = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String scheduleQuery = "SELECT s.schedulee_ID, e.e_first_name, e.e_last_name, r.room_ID, s.cleaning_date, s.status " +
                    "FROM schedulee s " +
                    "JOIN housekeeping h ON s.employee_ID = h.employee_ID " +
                    "JOIN employee_name e ON e.employee_ID = h.employee_ID " +
                    "JOIN room r ON r.room_ID = s.room_ID";
            PreparedStatement stmt = conn.prepareStatement(scheduleQuery);
            ResultSet rs = stmt.executeQuery();

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Schedule ID");
            columnNames.add("Housekeeper Name");
            columnNames.add("Room ID");
            columnNames.add("Cleaning Date");
            columnNames.add("Status");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("schedulee_ID"));
                row.add(rs.getString("e_first_name") + " " + rs.getString("e_last_name"));
                row.add(rs.getInt("room_ID"));
                row.add(rs.getDate("cleaning_date"));
                row.add(rs.getString("status"));
                data.add(row);
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            JOptionPane.showMessageDialog(this, scrollPane, "Housekeeping Schedule", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching housekeeping schedule: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void handleBookingConfirmation() {
        String query = "SELECT * FROM booking WHERE status = 'pending'";
        String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
        String username = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Booking ID");
            columnNames.add("Guest ID");
            columnNames.add("Room ID");
            columnNames.add("Check-In Date");
            columnNames.add("Check-Out Date");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("booking_ID"));
                row.add(rs.getInt("guest_ID"));
                row.add(rs.getInt("room_ID"));
                row.add(rs.getDate("check_in"));
                row.add(rs.getDate("check_out"));
                data.add(row);
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            int result = JOptionPane.showConfirmDialog(this, scrollPane, "Confirm/Deny Bookings", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String bookingIdInput = JOptionPane.showInputDialog("Enter Booking ID to Confirm or Deny:");
                String status = JOptionPane.showInputDialog("Enter 'confirmed' to approve: "); //SEÇENEKLİ LİSTEYE DEĞİŞTİRİLEBİLİR

                int bookingId = Integer.parseInt(bookingIdInput);
                String updateQuery = "UPDATE booking_requests SET b_status = ? WHERE booking_ID = ?";

                try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                    pstmt.setString(1, status);
                    pstmt.setInt(2, bookingId);
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Booking ID " + bookingId + " has been updated to " + status);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error handling booking confirmations: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewAvailableRooms() {
        String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
        String username = "root";
        String password = "";

        String query = "SELECT r.room_ID, rt.type_name, r.isClean, r.isAvailable FROM room r " +
                "JOIN room_type rt ON r.type_ID = rt.type_ID WHERE r.isAvailable = TRUE";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Room ID");
            columnNames.add("Room Type");
            columnNames.add("Cleanliness");
            columnNames.add("Available");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("room_ID"));
                row.add(rs.getString("type_name"));
                row.add(rs.getString("isClean"));
                row.add(rs.getBoolean("isAvailable") ? "Yes" : "No");
                data.add(row);
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            JOptionPane.showMessageDialog(this, scrollPane, "Available Rooms", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching available rooms: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void viewAllBookings() {
        String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
        String username = "root";
        String password = "";

        String query = "SELECT b.booking_ID, g.g_first_name, g.g_last_name, b.room_ID, b.check_in, b.check_out, b.b_status " +
                "FROM booking b " +
                "JOIN guest_name g ON b.guest_ID = g.guest_ID ORDER BY b.check_in";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Booking ID");
            columnNames.add("Guest Name");
            columnNames.add("Room ID");
            columnNames.add("Check-In Date");
            columnNames.add("Check-Out Date");
            columnNames.add("Booking Status");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("booking_ID"));
                row.add(rs.getString("g_first_name") + " " + rs.getString("g_last_name"));
                row.add(rs.getInt("room_ID"));
                row.add(rs.getDate("check_in"));
                row.add(rs.getDate("check_out"));
                row.add(rs.getString("b_status"));
                data.add(row);
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            JOptionPane.showMessageDialog(this, scrollPane, "All Bookings", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching all bookings: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void assignHousekeepingTask() {
        String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
        String username = "root";
        String password = "";

        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String housekeepersQuery = "SELECT e.employee_ID, e_name.e_first_name, e_name.e_last_name FROM housekeeping h " +
                    "JOIN employee_name e_name ON h.employee_ID = e_name.employee_ID";
            String roomsQuery = "SELECT room_ID FROM room WHERE isClean = 'Dirty'";
            PreparedStatement housekeepersStmt = conn.prepareStatement(housekeepersQuery);
            PreparedStatement roomsStmt = conn.prepareStatement(roomsQuery);

            ResultSet housekeepersRs = housekeepersStmt.executeQuery();
            ResultSet roomsRs = roomsStmt.executeQuery();

            Vector<String> housekeepers = new Vector<>();
            while (housekeepersRs.next()) {
                housekeepers.add(housekeepersRs.getInt("employee_ID") + ": " + housekeepersRs.getString("e_first_name") + " " + housekeepersRs.getString("e_last_name"));
            }

            Vector<String> dirtyRooms = new Vector<>();
            while (roomsRs.next()) {
                dirtyRooms.add(String.valueOf(roomsRs.getInt("room_ID")));
            }

            String selectedHousekeeper = (String) JOptionPane.showInputDialog(
                    this, "Select a Housekeeper:", "Assign Task", JOptionPane.QUESTION_MESSAGE,
                    null, housekeepers.toArray(), null);

            String selectedRoom = (String) JOptionPane.showInputDialog(
                    this, "Select a Room to Assign:", "Assign Task", JOptionPane.QUESTION_MESSAGE,
                    null, dirtyRooms.toArray(), null);

            if (selectedHousekeeper != null && selectedRoom != null) {
                int housekeeperId = Integer.parseInt(selectedHousekeeper.split(":")[0]);
                int roomId = Integer.parseInt(selectedRoom);

                String insertQuery = "INSERT INTO schedulee (employee_ID, room_ID, cleaning_date, status) VALUES (?, ?, CURDATE(), 'Not Started')";
                PreparedStatement assignTaskStmt = conn.prepareStatement(insertQuery);
                assignTaskStmt.setInt(1, housekeeperId);
                assignTaskStmt.setInt(2, roomId);

                int rowsInserted = assignTaskStmt.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Task assigned successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to assign task.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error assigning task: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void viewHousekeepersAvailability() {
        String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
        String username = "root";
        String password = "";

        String query = "SELECT e.employee_ID, e_name.e_first_name, e_name.e_last_name, h.availability FROM housekeeping h " +
                "JOIN employee_name e_name ON h.employee_ID = e_name.employee_ID";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Housekeeper ID");
            columnNames.add("Housekeeper Name");
            columnNames.add("Availability");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("employee_ID"));
                row.add(rs.getString("e_first_name") + " " + rs.getString("e_last_name"));
                row.add(rs.getBoolean("availability") ? "Available" : "Not Available");
                data.add(row);
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            JOptionPane.showMessageDialog(this, scrollPane, "Housekeepers Availability", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching housekeepers' availability: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void processPayment() { //biraz sıkıntılı, sonra tekrar bakılacak
        String bookingIdInput = JOptionPane.showInputDialog("Enter Booking ID for Payment Processing:");

        try {
            int bookingId = Integer.parseInt(bookingIdInput);

            String roomPriceQuery = "SELECT r.price FROM room_type r JOIN rooms r ON b.room_ID = r.room_ID WHERE b.booking_ID = ?";
            String updateQuery = "UPDATE payment SET amount = ?, status = 'confirmed' WHERE booking_id = ?";
            String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
            String username = "root";
            String password = "";

            try (Connection conn = DriverManager.getConnection(url, username, password);
                 PreparedStatement priceStmt = conn.prepareStatement(roomPriceQuery);
                 PreparedStatement paymentStmt = conn.prepareStatement(updateQuery)) {

                priceStmt.setInt(1, bookingId);
                ResultSet rs = priceStmt.executeQuery();

                if (rs.next()) {
                    double roomPrice = rs.getDouble("price");

                    double paymentAmount;
                    do {
                        String paymentAmountInput = JOptionPane.showInputDialog("Enter Payment Amount (Room Price: " + roomPrice + "):");
                        paymentAmount = Double.parseDouble(paymentAmountInput);

                        if (paymentAmount != roomPrice) {
                            JOptionPane.showMessageDialog(this, "Payment amount must match the room price. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } while (paymentAmount != roomPrice);

                    paymentStmt.setDouble(1, paymentAmount);
                    paymentStmt.setInt(2, bookingId);

                    int rowsUpdated = paymentStmt.executeUpdate();

                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(this, "Payment successfully processed for Booking ID: " + bookingId, "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to process payment. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Booking ID not found or invalid.", "Error", JOptionPane.ERROR_MESSAGE);
                }

            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid input for Booking ID or Payment Amount.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewBookingRequests() {
        String url = "jdbc:mysql://localhost:3306/hotel_management_system1";
        String username = "root";
        String password = "";

        String query = "SELECT b.booking_ID, g.g_first_name, g.g_last_name, b.room_ID, b.check_in, b.check_out, b.b_status " +
                "FROM booking b " +
                "JOIN guest_name g ON b.guest_ID = g.guest_ID ORDER BY b.check_in";
        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            Vector<String> columnNames = new Vector<>();
            columnNames.add("Booking ID");
            columnNames.add("Guest Name");
            columnNames.add("Room ID");
            columnNames.add("Check-In Date");
            columnNames.add("Check-Out Date");
            columnNames.add("Status");

            Vector<Vector<Object>> data = new Vector<>();
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getInt("booking_ID"));
                row.add(rs.getString("g_first_name") + " " + rs.getString("g_last_name"));
                row.add(rs.getInt("room_ID"));
                row.add(rs.getDate("check_in"));
                row.add(rs.getDate("check_out"));
                row.add(rs.getString("b_status"));
                data.add(row);
            }

            JTable table = new JTable(data, columnNames);
            JScrollPane scrollPane = new JScrollPane(table);

            JOptionPane.showMessageDialog(this, scrollPane, "Booking Requests", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error fetching booking requests: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

}