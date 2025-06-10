import java.sql.*;
import java.util.*;
import java.io.*;
import javax.sound.sampled.*;
import java.util.Random;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public class HotelReservationSystem {
    Scanner sc = new Scanner(System.in);

    public void reserveRoom(Connection con) {
        try {
            System.out.println("Enter the Guest Name:");
            String name = sc.nextLine().trim();

            if (!name.matches("^[a-zA-Z ]+$")) {
                System.out.println("Invalid name. Please enter only letters and spaces.");
                return;
            }

            System.out.println("Enter the Guest Email:");
            String email = sc.nextLine().trim();

            if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
                System.out.println("Invalid email format. Please enter a valid email.");
                return;
            }

            System.out.println("Enter the Guest Contact Number:");
            String contactNo = sc.nextLine().trim();

            if (!contactNo.matches("^[0-9]{10}$")) {
                System.out.println("Invalid contact number. It must be exactly 10 digits.");
                return;
            }

            int guestId = -1;

            String checkGuestQuery = "SELECT guest_id FROM guests WHERE email = ? OR contact_no = ?";
            try (PreparedStatement pstmt = con.prepareStatement(checkGuestQuery)) {
                pstmt.setString(1, email);
                pstmt.setString(2, contactNo);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        guestId = rs.getInt("guest_id");
                        System.out.println("Existing guest found with ID: " + guestId);
                    } else {
                        System.out.println("New guest detected. Please enter the address:");
                        String address = sc.nextLine();

                        String insertGuestQuery = "INSERT INTO guests (full_name, email, contact_no, address) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement insertPstmt = con.prepareStatement(insertGuestQuery, Statement.RETURN_GENERATED_KEYS)) {
                            insertPstmt.setString(1, name);
                            insertPstmt.setString(2, email);
                            insertPstmt.setString(3, contactNo);
                            insertPstmt.setString(4, address);
                            insertPstmt.executeUpdate();
                            ResultSet newGuestRS = insertPstmt.getGeneratedKeys();
                            if (newGuestRS.next()) {
                                guestId = newGuestRS.getInt(1);
                                System.out.println("New guest added with ID: " + guestId);
                            }
                        }
                    }
                }
            }

            if (guestId == -1) {
                System.out.println("Failed to get guest ID. Reservation cancelled.");
                return;
            }

            String availableRoomsQuery = "SELECT room_id, room_number, room_type, price_per_night FROM rooms WHERE status = 'Available'";
            int roomId = -1;
            double roomPrice = 0;
            try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(availableRoomsQuery)) {
                System.out.println("\n--- Available Rooms ---");
                System.out.printf("%-10s | %-15s | %-15s | %-10s\n", "Room ID", "Room Number", "Room Type", "Price");
                System.out.println("------------------------------------------------------------");
                while (rs.next()) {
                    System.out.printf("%-10d | %-15s | %-15s | %-10.2f\n",
                            rs.getInt("room_id"),
                            rs.getString("room_number"),
                            rs.getString("room_type"),
                            rs.getDouble("price_per_night"));
                }
                System.out.println("------------------------------------------------------------");
            }

            String roomNo;
            boolean roomSelected = false;
            while (!roomSelected) {
                System.out.println("Enter the room number you want to book:");
                roomNo = sc.nextLine();
                String checkRoomQuery = "SELECT room_id, price_per_night FROM rooms WHERE room_number = ? AND status = 'Available'";
                try (PreparedStatement checkPstmt = con.prepareStatement(checkRoomQuery)) {
                    checkPstmt.setString(1, roomNo);
                    try (ResultSet rs = checkPstmt.executeQuery()) {
                        if (rs.next()) {
                            roomSelected = true;
                            roomId = rs.getInt("room_id");
                            roomPrice = rs.getDouble("price_per_night");
                            System.out.println("Room " + roomNo + " is available and selected.");
                        } else {
                            System.out.println("Room " + roomNo + " is not available! Please choose another room.");
                        }
                    }
                }
            }

            System.out.println("Enter Check-in Date (YYYY-MM-DD):");
            String checkInDateStr = sc.nextLine();
            System.out.println("Enter Check-out Date (YYYY-MM-DD):");
            String checkOutDateStr = sc.nextLine();

            LocalDate checkInDate = LocalDate.parse(checkInDateStr);
            LocalDate checkOutDate = LocalDate.parse(checkOutDateStr);

            if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
                System.out.println("Check-out date must be after check-in date.");
                return;
            }

            long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            double totalPrice = nights * roomPrice;

            System.out.println("Enter discount code (if any):");
            String discountCode = sc.nextLine();
            double discountPercent = 0;
            if (!discountCode.isEmpty()) {
                String discountQuery = "SELECT discount_percent FROM discounts WHERE code = ? AND expiry_date > NOW()";
                try (PreparedStatement discountPstmt = con.prepareStatement(discountQuery)) {
                    discountPstmt.setString(1, discountCode);
                    try (ResultSet rs = discountPstmt.executeQuery()) {
                        if (rs.next()) {
                            discountPercent = rs.getDouble("discount_percent");
                            System.out.println("Discount applied: " + discountPercent + "%");
                        } else {
                            System.out.println("Invalid or expired discount code.");
                        }
                    }
                }
            }
            double finalPrice = totalPrice - (totalPrice * (discountPercent / 100));
            int reservationId = 0;
            String insertReservationSql = "INSERT INTO reservations (guest_id, room_id, check_in, check_out, total_price, booking_status) VALUES (?, ?, ?, ?, ?, 'Confirmed')";
            try (PreparedStatement pstmt = con.prepareStatement(insertReservationSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, guestId);
                pstmt.setInt(2, roomId);
                pstmt.setString(3, checkInDateStr);
                pstmt.setString(4, checkOutDateStr);
                pstmt.setDouble(5, finalPrice);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        reservationId = rs.getInt(1);
                        System.out.println("\n--- Reservation Successful! ---");
                        System.out.println("Reservation ID: " + reservationId);
                        System.out.printf("Total Price: %.2f\n", finalPrice);
                    }
                } else {
                    System.out.println("Reservation Failed.");
                    return;
                }
            }

            String insertPaymentSql = "INSERT INTO payments (reservation_id, amount, payment_method, payment_status) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(insertPaymentSql)) {
                pstmt.setInt(1, reservationId);
                pstmt.setDouble(2, finalPrice);
                pstmt.setString(3, "Credit Card");
                pstmt.setString(4, "Completed");
                pstmt.executeUpdate();
                System.out.println("Payment recorded successfully.");
            }

            String updateRoomStatusSql = "UPDATE rooms SET status = 'Occupied' WHERE room_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateRoomStatusSql)) {
                pstmt.setInt(1, roomId);
                pstmt.executeUpdate();
                System.out.println("Room status updated to 'Occupied'.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void viewReserve(Connection con) {
        try {
            String sql = "SELECT * FROM reservations";
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            System.out.println("\n--- All Reservations ---");
            System.out.println("-------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-5s | %-10s | %-10s | %-12s | %-12s | %-12s | %-15s | %-20s |\n",
                    "ID", "Guest ID", "Room ID", "Check-in", "Check-out", "Total Price", "Booking Status", "Reservation Date");
            System.out.println("-------------------------------------------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("| %-5d | %-10d | %-10d | %-12s | %-12s | %-12.2f | %-15s | %-20s |\n",
                        rs.getInt("reservation_id"),
                        rs.getInt("guest_id"),
                        rs.getInt("room_id"),
                        rs.getDate("check_in"),
                        rs.getDate("check_out"),
                        rs.getDouble("total_price"),
                        rs.getString("booking_status"),
                        rs.getTimestamp("reservation_date"));
            }
            System.out.println("-------------------------------------------------------------------------------------------------------------------");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void getRoomNumber(Connection connection) {
        try {
            System.out.print("Enter reservation ID: ");
            int reservationId = sc.nextInt();
            sc.nextLine(); // consume newline
            System.out.print("Enter guest ID: ");
            int guestId = sc.nextInt();
            sc.nextLine(); // consume newline

            String sql = "SELECT r.room_number FROM reservations res JOIN rooms r ON res.room_id = r.room_id WHERE res.reservation_id = ? AND res.guest_id = ?";

            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, reservationId);
                pstmt.setInt(2, guestId);
                try (ResultSet resultSet = pstmt.executeQuery()) {
                    if (resultSet.next()) {
                        String roomNumber = resultSet.getString("room_number");
                        System.out.println("Room number for Reservation ID " + reservationId + " and Guest ID " + guestId + " is: " + roomNumber);
                    } else {
                        System.out.println("Reservation not found for the given ID and guest ID.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void updateGuestDetails(Connection connection) {
        try {
            System.out.print("Enter reservation ID: ");
            int reservationId = sc.nextInt();
            sc.nextLine(); // consume newline

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            int guestId = getGuestIdByReservation(connection, reservationId);
            if (guestId == -1) {
                System.out.println("Error retrieving guest information.");
                return;
            }

            System.out.print("Enter new full name (press Enter to skip): ");
            String newFullName = sc.nextLine();
            System.out.print("Enter new email (press Enter to skip): ");
            String newEmail = sc.nextLine();
            System.out.print("Enter new address (press Enter to skip): ");
            String newAddress = sc.nextLine();

            StringBuilder sql = new StringBuilder("UPDATE guests SET ");
            boolean hasChanges = false;

            if (!newFullName.isEmpty()) {
                sql.append("full_name = ?, ");
                hasChanges = true;
            }
            if (!newEmail.isEmpty()) {
                sql.append("email = ?, ");
                hasChanges = true;
            }
            if (!newAddress.isEmpty()) {
                sql.append("address = ?, ");
                hasChanges = true;
            }

            if (!hasChanges) {
                System.out.println("No changes provided.");
                return;
            }

            sql.setLength(sql.length() - 2); // Remove trailing ", "
            sql.append(" WHERE guest_id = ?");

            try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
                int paramIndex = 1;
                if (!newFullName.isEmpty()) {
                    pstmt.setString(paramIndex++, newFullName);
                }
                if (!newEmail.isEmpty()) {
                    pstmt.setString(paramIndex++, newEmail);
                }
                if (!newAddress.isEmpty()) {
                    pstmt.setString(paramIndex++, newAddress);
                }
                pstmt.setInt(paramIndex, guestId);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Guest details updated successfully!");
                } else {
                    System.out.println("Guest details update failed.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void deleteReservation(Connection connection) {
        try {
            System.out.print("Enter reservation ID to delete: ");
            if (!sc.hasNextInt()) {
                System.out.println("Invalid input. Please enter a valid numeric reservation ID.");
                sc.next(); // consume the invalid input
                return;
            }
            int reservationId = sc.nextInt();
            sc.nextLine(); // consume newline

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            String getRoomIdQuery = "SELECT room_id FROM reservations WHERE reservation_id = ?";
            int roomId = -1;
            try (PreparedStatement pstmt = connection.prepareStatement(getRoomIdQuery)) {
                pstmt.setInt(1, reservationId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        roomId = rs.getInt("room_id");
                    }
                }
            }

            String deleteReservationQuery = "DELETE FROM reservations WHERE reservation_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(deleteReservationQuery)) {
                pstmt.setInt(1, reservationId);
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    System.out.println("Reservation deleted successfully!");

                    if (roomId != -1) {
                        String updateRoomStatusSql = "UPDATE rooms SET status = 'Available' WHERE room_id = ?";
                        try (PreparedStatement updatePstmt = connection.prepareStatement(updateRoomStatusSql)) {
                            updatePstmt.setInt(1, roomId);
                            updatePstmt.executeUpdate();
                            System.out.println("Room status updated to 'Available'.");
                        }
                    }

                    // Check if the guest has any other reservations
                    String getGuestIdQuery = "SELECT guest_id FROM reservations WHERE reservation_id = ?";
                    int guestId = -1;
                    try (PreparedStatement pstmtGuest = connection.prepareStatement(getGuestIdQuery)) {
                        pstmtGuest.setInt(1, reservationId);
                        try (ResultSet rsGuest = pstmtGuest.executeQuery()) {
                            if (rsGuest.next()) {
                                guestId = rsGuest.getInt("guest_id");
                            }
                        }
                    }

                    if (guestId != -1) {
                        String countReservationsQuery = "SELECT COUNT(*) FROM reservations WHERE guest_id = ?";
                        try (PreparedStatement countPstmt = connection.prepareStatement(countReservationsQuery)) {
                            countPstmt.setInt(1, guestId);
                            try (ResultSet rs = countPstmt.executeQuery()) {
                                if (rs.next() && rs.getInt(1) == 0) {
                                    String deleteGuestQuery = "DELETE FROM guests WHERE guest_id = ?";
                                    try (PreparedStatement deleteGuestPstmt = connection.prepareStatement(deleteGuestQuery)) {
                                        deleteGuestPstmt.setInt(1, guestId);
                                        int guestDeleted = deleteGuestPstmt.executeUpdate();
                                        if (guestDeleted > 0) {
                                            System.out.println("Guest record deleted successfully (no other reservations).");
                                        }
                                    }
                                }
                            }
                        }
                    }

                } else {
                    System.out.println("Reservation deletion failed.");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    private int getGuestIdByReservation(Connection connection, int reservationId) {
        String sql = "SELECT guest_id FROM reservations WHERE reservation_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            try (ResultSet resultSet = pstmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("guest_id");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return -1;
    }

    public static void exit() throws InterruptedException {
        System.out.print("Exiting the System");
        int i = 5;
        while (i != 0) {
            System.out.print(".");
            Thread.sleep(450);
            i--;
        }
        System.out.println();
        System.out.println("Thank you for Visiting the Hotel");
    }

    private static boolean reservationExists(Connection connection, int reservationId) {
        try {
            String sql = "SELECT reservation_id FROM reservations WHERE reservation_id = ?";
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, reservationId);
                try (ResultSet resultSet = pstmt.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }


    public static void viewavailable(Connection con) {
        try {
            int consoleWidth = getTerminalWidth();
            Statement stmt = con.createStatement();
            String query = "SELECT room_type, COUNT(*) AS available_rooms FROM rooms WHERE status = 'Available' GROUP BY room_type";
            ResultSet rs = stmt.executeQuery(query);

            printCentered("\n--- Number of Available Rooms per Category ---", consoleWidth);
            while (rs.next()) {
                printCentered(rs.getString("room_type") + ": " + rs.getInt("available_rooms"), consoleWidth);
            }

            String[] roomTypes = {"Single", "Double", "Suite", "Deluxe"};
            for (String type : roomTypes) {
                System.out.println("\n--- Available " + type + " Rooms Sorted by Price ---");
                String roomQuery = "SELECT room_number, price_per_night FROM rooms WHERE status = 'Available' AND room_type = ? ORDER BY price_per_night ASC";
                try (PreparedStatement pstmt = con.prepareStatement(roomQuery)) {
                    pstmt.setString(1, type);
                    ResultSet roomRs = pstmt.executeQuery();
                    System.out.printf("%-15s | %-15s\n", "Room Number", "Price");
                    System.out.println("--------------------------------");
                    while (roomRs.next()) {
                        System.out.printf("%-15s | %-15.2f\n", roomRs.getString("room_number"), roomRs.getDouble("price_per_night"));
                    }
                    System.out.println("--------------------------------");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    public static void clearScreen() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            System.out.println("Error clearing screen: " + e.getMessage());
        }
    }

    public static void checkIn(Connection con) {
        try {
            Scanner sc = new Scanner(System.in);
            Statement stmt = con.createStatement();

            // Get guest details
            System.out.print("Enter Contact Number: ");
            String contact = sc.nextLine().trim();
            while (contact.length() == 0 || !contact.matches("^[0-9]{10}$")) {
                System.out.println("Please enter a valid 10-digit contact number.");
                System.out.print("Enter Contact Number: ");
                contact = sc.nextLine().trim();
            }

            // Check if guest exists
            String checkGuestQuery = "SELECT guest_id, full_name FROM guests WHERE contact_no = ?";
            int guestId;
            String guestName = "";

            try (PreparedStatement pstmt = con.prepareStatement(checkGuestQuery)) {
                pstmt.setString(1, contact);
                try (ResultSet guestRs = pstmt.executeQuery()) {
                    if (!guestRs.next()) {
                        // New guest registration
                        System.out.print("Enter Guest Name: ");
                        guestName = sc.nextLine().trim();
                        System.out.print("Enter Guest Email: ");
                        String email = sc.nextLine().trim();
                        System.out.print("Enter Guest Address: ");
                        String address = sc.nextLine().trim();

                        String insertGuestQuery = "INSERT INTO guests (full_name, email, contact_no, address) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement insertPstmt = con.prepareStatement(insertGuestQuery, Statement.RETURN_GENERATED_KEYS)) {
                            insertPstmt.setString(1, guestName);
                            insertPstmt.setString(2, email);
                            insertPstmt.setString(3, contact);
                            insertPstmt.setString(4, address);
                            insertPstmt.executeUpdate();
                            ResultSet generatedKeys = insertPstmt.getGeneratedKeys();

                            if (generatedKeys.next()) {
                                guestId = generatedKeys.getInt(1);
                                System.out.println("New guest registered successfully!");
                            } else {
                                throw new SQLException("Failed to create new guest record.");
                            }
                        }
                    } else {
                        guestId = guestRs.getInt("guest_id");
                        guestName = guestRs.getString("full_name");
                    }
                }
            }


            System.out.print("Enter ID Proof: ");
            String idProof = sc.nextLine().trim();
            String checkReservationQuery = "SELECT r.room_number, res.reservation_id, r.price_per_night, res.check_in, res.check_out FROM reservations res " +
                    "JOIN rooms r ON res.room_id = r.room_id " +
                    "WHERE res.guest_id = ? AND res.booking_status = 'Confirmed' LIMIT 1";
            String assignedRoom = "";
            int reservationId = -1;
            double totalPrice = 0.0;
            LocalDate checkInDate = null;
            LocalDate checkOutDate = null;

            try (PreparedStatement pstmt = con.prepareStatement(checkReservationQuery)) {
                pstmt.setInt(1, guestId);
                try (ResultSet resRs = pstmt.executeQuery()) {
                    if (resRs.next()) {
                        assignedRoom = resRs.getString("room_number");
                        reservationId = resRs.getInt("reservation_id");
                        totalPrice = resRs.getDouble("price_per_night"); // This is just the price per night, not total
                        checkInDate = resRs.getDate("check_in").toLocalDate();
                        checkOutDate = resRs.getDate("check_out").toLocalDate();
                        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
                        totalPrice = nights * totalPrice; // Calculate actual total price
                        System.out.println("Found your reservation. Your assigned room is: " + assignedRoom);
                    } else {
                        // Handle walk-in check-in
                        System.out.println("No existing reservation found. Processing walk-in check-in...");
                        boolean validRoomSelected = false;

                        while (!validRoomSelected) {
                            System.out.print("Enter the room number you want to check in: ");
                            assignedRoom = sc.nextLine().trim();

                            String checkRoomQuery = "SELECT room_id, price_per_night FROM rooms WHERE room_number = ? AND status = 'Available'";
                            try (PreparedStatement roomPstmt = con.prepareStatement(checkRoomQuery)) {
                                roomPstmt.setString(1, assignedRoom);
                                try (ResultSet roomRs = roomPstmt.executeQuery()) {
                                    if (roomRs.next()) {
                                        int roomId = roomRs.getInt("room_id");
                                        double pricePerNight = roomRs.getDouble("price_per_night");

                                        // For walk-in, set check-in to today and check-out to tomorrow by default
                                        checkInDate = LocalDate.now();
                                        checkOutDate = LocalDate.now().plusDays(1);
                                        totalPrice = pricePerNight; // For one night

                                        String insertReservationQuery = "INSERT INTO reservations (guest_id, room_id, check_in, check_out, total_price, booking_status) VALUES (?, ?, ?, ?, ?, 'Checked-in')";
                                        try (PreparedStatement insertReservationPstmt = con.prepareStatement(insertReservationQuery, Statement.RETURN_GENERATED_KEYS)) {
                                            insertReservationPstmt.setInt(1, guestId);
                                            insertReservationPstmt.setInt(2, roomId);
                                            insertReservationPstmt.setString(3, checkInDate.toString());
                                            insertReservationPstmt.setString(4, checkOutDate.toString());
                                            insertReservationPstmt.setDouble(5, totalPrice);
                                            insertReservationPstmt.executeUpdate();
                                            ResultSet generatedKeys = insertReservationPstmt.getGeneratedKeys();

                                            if (generatedKeys.next()) {
                                                reservationId = generatedKeys.getInt(1);
                                                validRoomSelected = true;
                                            }
                                        }
                                    } else {
                                        System.out.println("Invalid room number or the room is not available. Please try again.");
                                    }
                                }
                            }
                        }
                    }
                }
            }


            // Mark reservation as checked-in and update room status
            String updateReservationQuery = "UPDATE reservations SET booking_status = 'Checked-in' WHERE reservation_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateReservationQuery)) {
                pstmt.setInt(1, reservationId);
                pstmt.executeUpdate();
            }

            String updateRoomQuery = "UPDATE rooms SET status = 'Occupied' WHERE room_number = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateRoomQuery)) {
                pstmt.setString(1, assignedRoom);
                pstmt.executeUpdate();
            }

            // Insert check-in record
            String insertCheckinQuery = "INSERT INTO checkins (guest_name, contact, id_proof, room_number, checkin_date) VALUES (?, ?, ?, ?, NOW())";
            try (PreparedStatement pstmt = con.prepareStatement(insertCheckinQuery)) {
                pstmt.setString(1, guestName);
                pstmt.setString(2, contact);
                pstmt.setString(3, idProof);
                pstmt.setString(4, assignedRoom);
                pstmt.executeUpdate();
            }

            String insertPaymentQuery = "INSERT INTO payments (reservation_id, amount, payment_method, payment_status) VALUES (?, ?, 'Cash', 'Completed')";
            try (PreparedStatement pstmt = con.prepareStatement(insertPaymentQuery)) {
                pstmt.setInt(1, reservationId);
                pstmt.setDouble(2, totalPrice);
                pstmt.executeUpdate();
            }

            System.out.println("Check-in successful! Total price for stay: " + String.format("%.2f", totalPrice));
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    public static void paymentHistory(Connection con) {
        try {
            String sql = "SELECT p.payment_id, p.reservation_id, g.full_name, r.room_number, p.amount, p.payment_method, p.payment_status, p.payment_date " +
                    "FROM payments p " +
                    "JOIN reservations res ON p.reservation_id = res.reservation_id " +
                    "JOIN guests g ON res.guest_id = g.guest_id " +
                    "JOIN rooms r ON res.room_id = r.room_id " +
                    "ORDER BY p.payment_date DESC";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            System.out.println("\n--- Payment History ---");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("| %-10s | %-14s | %-20s | %-12s | %-10s | %-15s | %-15s | %-20s |\n",
                    "Payment ID", "Reservation ID", "Guest Name", "Room No.", "Amount", "Method", "Status", "Payment Date");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

            while (rs.next()) {
                System.out.printf("| %-10d | %-14d | %-20s | %-12s | %-10.2f | %-15s | %-15s | %-20s |\n",
                        rs.getInt("payment_id"),
                        rs.getInt("reservation_id"),
                        rs.getString("full_name"),
                        rs.getString("room_number"),
                        rs.getDouble("amount"),
                        rs.getString("payment_method"),
                        rs.getString("payment_status"),
                        rs.getTimestamp("payment_date"));
            }
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    public static void emergencyReporting(Connection con) {
        Scanner sc = new Scanner(System.in);
        int consoleWidth = getTerminalWidth();

        printCentered("--- Emergency Reporting ---", consoleWidth);
        System.out.println("Please describe the emergency situation.");
        System.out.print("Description: ");
        String description = sc.nextLine();

        System.out.print("Enter Room Number (if applicable, press Enter to skip): ");
        String roomNumber = sc.nextLine().trim();

        System.out.print("Enter Guest Name (if known, press Enter to skip): ");
        String guestName = sc.nextLine().trim();

        String insertEmergencyQuery = "INSERT INTO emergencies (description, room_number, guest_name, report_time) VALUES (?, ?, ?, NOW())";

        try (PreparedStatement pstmt = con.prepareStatement(insertEmergencyQuery)) {
            pstmt.setString(1, description);
            if (roomNumber.isEmpty()) {
                pstmt.setNull(2, Types.VARCHAR);
            } else {
                pstmt.setString(2, roomNumber);
            }
            if (guestName.isEmpty()) {
                pstmt.setNull(3, Types.VARCHAR);
            } else {
                pstmt.setString(3, guestName);
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Emergency report successfully submitted!");
                System.out.println("Hotel staff has been notified and will respond shortly.");
            } else {
                System.out.println("Failed to submit emergency report.");
            }
        } catch (SQLException e) {
            System.out.println("Error submitting emergency report: " + e.getMessage());
        }
    }


    public static void groupReservations(Connection con) {
        Scanner sc = new Scanner(System.in);
        int consoleWidth = getTerminalWidth();

        printCentered("--- Group Reservations ---", consoleWidth);

        System.out.print("Enter Group Name: ");
        String groupName = sc.nextLine();

        System.out.print("Enter Number of Rooms required: ");
        if (!sc.hasNextInt()) {
            System.out.println("Invalid input. Please enter a valid number.");
            sc.next(); // consume invalid input
            return;
        }
        int numberOfRooms = sc.nextInt();
        sc.nextLine(); // consume newline

        System.out.print("Enter Check-in Date (YYYY-MM-DD): ");
        String checkInDateStr = sc.nextLine();

        System.out.print("Enter Check-out Date (YYYY-MM-DD): ");
        String checkOutDateStr = sc.nextLine();

        LocalDate checkInDate = LocalDate.parse(checkInDateStr);
        LocalDate checkOutDate = LocalDate.parse(checkOutDateStr);

        if (checkOutDate.isBefore(checkInDate) || checkOutDate.isEqual(checkInDate)) {
            System.out.println("Check-out date must be after check-in date.");
            return;
        }
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);

        List<Integer> availableRoomIds = new ArrayList<>();
        List<String> availableRoomNumbers = new ArrayList<>();
        List<Double> availableRoomPrices = new ArrayList<>();

        String availableRoomsQuery = "SELECT room_id, room_number, price_per_night FROM rooms WHERE status = 'Available' LIMIT ?";
        try (PreparedStatement pstmt = con.prepareStatement(availableRoomsQuery)) {
            pstmt.setInt(1, numberOfRooms);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    availableRoomIds.add(rs.getInt("room_id"));
                    availableRoomNumbers.add(rs.getString("room_number"));
                    availableRoomPrices.add(rs.getDouble("price_per_night"));
                }
            }
        } catch (SQLException e) {
            System.out.println("Error checking available rooms: " + e.getMessage());
            return;
        }

        if (availableRoomIds.size() < numberOfRooms) {
            System.out.println("Not enough available rooms for the group reservation. Only " + availableRoomIds.size() + " rooms are available.");
            return;
        }

        double totalGroupPrice = 0.0;
        int groupReservationId = 0;

        try {
            con.setAutoCommit(false); // Start transaction

            // Insert a placeholder reservation to get a reservation_id for the group
            String insertGroupReservationSql = "INSERT INTO reservations (guest_id, room_id, check_in, check_out, total_price, booking_status) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = con.prepareStatement(insertGroupReservationSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setNull(1, Types.INTEGER); // No specific guest ID for the group placeholder
                pstmt.setNull(2, Types.INTEGER); // No specific room ID for the group placeholder
                pstmt.setString(3, checkInDateStr);
                pstmt.setString(4, checkOutDateStr);
                pstmt.setDouble(5, 0.0); // Temporary total price
                pstmt.setString(6, "Group Pending");
                pstmt.executeUpdate();
                ResultSet generatedKeys = pstmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    groupReservationId = generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Failed to create group reservation placeholder.");
                }
            }

            // Assign rooms and create individual reservations for each room in the group
            for (int i = 0; i < numberOfRooms; i++) {
                int roomId = availableRoomIds.get(i);
                double roomPrice = availableRoomPrices.get(i);
                double currentRoomTotalPrice = nights * roomPrice;
                totalGroupPrice += currentRoomTotalPrice;

                // Create a dummy guest for each room in the group or link to existing if a group leader exists
                // For simplicity, let's assume a new dummy guest for each room for now.
                // In a real system, you might have a "group leader" guest or dynamically add guests.
                String insertGuestQuery = "INSERT INTO guests (full_name, email, contact_no, address) VALUES (?, ?, ?, ?)";
                int currentGuestId = -1;
                try (PreparedStatement guestPstmt = con.prepareStatement(insertGuestQuery, Statement.RETURN_GENERATED_KEYS)) {
                    guestPstmt.setString(1, groupName + " Guest " + (i + 1));
                    guestPstmt.setString(2, "group_guest_" + (i + 1) + "@example.com");
                    guestPstmt.setString(3, "0000000" + (100 + i)); // Dummy contact
                    guestPstmt.setString(4, "Group Address");
                    guestPstmt.executeUpdate();
                    ResultSet generatedKeys = guestPstmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        currentGuestId = generatedKeys.getInt(1);
                    }
                }

                String insertRoomReservationSql = "INSERT INTO reservations (guest_id, room_id, check_in, check_out, total_price, booking_status, group_reservation_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = con.prepareStatement(insertRoomReservationSql)) {
                    pstmt.setInt(1, currentGuestId);
                    pstmt.setInt(2, roomId);
                    pstmt.setString(3, checkInDateStr);
                    pstmt.setString(4, checkOutDateStr);
                    pstmt.setDouble(5, currentRoomTotalPrice);
                    pstmt.setString(6, "Confirmed");
                    pstmt.setInt(7, groupReservationId); // Link to the main group reservation
                    pstmt.executeUpdate();
                }

                String updateRoomStatusSql = "UPDATE rooms SET status = 'Occupied' WHERE room_id = ?";
                try (PreparedStatement pstmt = con.prepareStatement(updateRoomStatusSql)) {
                    pstmt.setInt(1, roomId);
                    pstmt.executeUpdate();
                }
            }

            // Update the main group reservation with the final total price and status
            String updateGroupReservationSql = "UPDATE reservations SET total_price = ?, booking_status = 'Confirmed' WHERE reservation_id = ?";
            try (PreparedStatement pstmt = con.prepareStatement(updateGroupReservationSql)) {
                pstmt.setDouble(1, totalGroupPrice);
                pstmt.setInt(2, groupReservationId);
                pstmt.executeUpdate();
            }

            con.commit(); // Commit transaction
            System.out.println("\n--- Group Reservation Successful! ---");
            System.out.println("Group Reservation ID: " + groupReservationId);
            System.out.println("Rooms booked: " + availableRoomNumbers);
            System.out.printf("Total Price for the group: %.2f\n", totalGroupPrice);

        } catch (SQLException e) {
            try {
                con.rollback(); // Rollback transaction if an error occurs
                System.out.println("Group reservation failed. Rolling back changes.");
            } catch (SQLException rollbackEx) {
                System.out.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                con.setAutoCommit(true); // Restore auto-commit mode
            } catch (SQLException e) {
                System.out.println("Error restoring auto-commit: " + e.getMessage());
            }
        }
    }


    public static void usermenu() {
        int consoleWidth = getTerminalWidth();

        printCentered("╔═══════════════════════════════════════════════════════════╗", consoleWidth);
        printCentered("║                WELCOME TO LUXURY HOTELS                   ║", consoleWidth);
        printCentered("╠═══════════════════════════════════════════════════════════╣", consoleWidth);
        printCentered("║                                                           ║", consoleWidth);
        printCentered("║   1.  MAKE RESERVATIONS         6.  VIEW AVAILABLE ROOMS  ║", consoleWidth);
        printCentered("║   2.  VIEW RESERVATIONS         7.  CHECK-IN GUEST        ║", consoleWidth);
        printCentered("║   3.  UPDATE RESERVATIONS       8.  PAYMENT HISTORY       ║", consoleWidth);
        printCentered("║   4.  FIND ROOM NO              9.  EMERGENCY REPORTING   ║", consoleWidth);
        printCentered("║   5.  DELETE RESERVATIONS       10. GROUP RESERVATIONS    ║", consoleWidth);
        printCentered("║                                  11. EXIT                 ║", consoleWidth);
        printCentered("║                                                           ║", consoleWidth);
        printCentered("╚═══════════════════════════════════════════════════════════╝", consoleWidth);
        System.out.print("\n" + " ".repeat((consoleWidth - 20) / 2) + "Enter your choice: ");

    }

    public static void main(String args[]) throws ClassNotFoundException, SQLException {
        String url = "jdbc:mysql://localhost:3306/hotel";
        String username = "root";
        String password = "7453962114";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.out.println("Error loading MySQL driver: " + e.getMessage());
            return;
        }

        try (Connection con = DriverManager.getConnection(url, username, password)) {
            HotelReservationSystem obj = new HotelReservationSystem();
            clearScreen();
            usermenu();
            while (true) {
                int n;
                try {
                    n = Integer.parseInt(obj.sc.nextLine()); // Use nextLine() and parse
                } catch (NumberFormatException e) {
                    clearScreen();
                    printCentered("Invalid Input. Please enter a number.", getTerminalWidth());
                    usermenu();
                    continue;
                }

                switch (n) {
                    case 1:
                        clearScreen();
                        obj.reserveRoom(con);
                        usermenu();
                        break;
                    case 2:
                        clearScreen();
                        obj.viewReserve(con);
                        usermenu();
                        break;
                    case 3:
                        clearScreen();
                        obj.updateGuestDetails(con);
                        usermenu();
                        break;
                    case 4:
                        clearScreen();
                        printCentered("=== ADMIN AUTHENTICATION REQUIRED ===", getTerminalWidth());
                        System.out.print("\n" + " ".repeat((getTerminalWidth() - 20) / 2) + "Enter Admin Name: ");
                        String name = obj.sc.nextLine();
                        System.out.print(" ".repeat((getTerminalWidth() - 20) / 2) + "Enter Admin ID: ");
                        String id = obj.sc.nextLine(); // Use nextLine()
                        System.out.print(" ".repeat((getTerminalWidth() - 20) / 2) + "Enter Passcode: ");
                        int passcode;
                        try {
                            passcode = Integer.parseInt(obj.sc.nextLine()); // Use nextLine() and parse
                        } catch (NumberFormatException e) {
                            printCentered("Invalid Passcode. Must be a number.", getTerminalWidth());
                            usermenu();
                            break;
                        }

                        if (name.equalsIgnoreCase("Vaibhav Khandelwal") &&
                                id.equalsIgnoreCase("7453962114") &&
                                passcode == 2408) {
                            obj.getRoomNumber(con);
                        } else {
                            printCentered("Access Denied! Returning to Main Menu...", getTerminalWidth());
                        }
                        usermenu();
                        break;
                    case 5:
                        clearScreen();
                        obj.deleteReservation(con);
                        usermenu();
                        break;
                    case 6:
                        clearScreen();
                        obj.viewavailable(con);
                        usermenu();
                        break;
                    case 7:
                        clearScreen();
                        obj.checkIn(con);
                        usermenu();
                        break;
                    case 8:
                        clearScreen();
                        obj.paymentHistory(con);
                        usermenu();
                        break;
                    case 9:
                        clearScreen();
                        obj.emergencyReporting(con);
                        usermenu();
                        break;
                    case 10:
                        clearScreen();
                        obj.groupReservations(con);
                        usermenu();
                        break;
                    case 11:
                        clearScreen();
                        try {
                            exit();
                        } catch (InterruptedException e) {
                            System.out.println("Error during exit: " + e.getMessage());
                        }
                        return;
                    default:
                        clearScreen();
                        printCentered("Invalid Input. Please Try Again.", getTerminalWidth());
                        usermenu();
                }
            }
        }
    }

    private static void printCentered(String text, int width) {
        int padding = (width - text.length()) / 2;
        if (padding < 0)
            padding = 0;
        System.out.println(" ".repeat(padding) + text);
    }

    private static int getTerminalWidth() {
        try {
            Process process = Runtime.getRuntime().exec("tput cols");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String result = reader.readLine();
            return (result != null && !result.isEmpty()) ? Integer.parseInt(result.trim()) : 80;
        } catch (Exception e) {
            return 120; // Default width if unable to determine
        }
    }
}