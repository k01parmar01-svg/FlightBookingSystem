import java.sql.*;
import java.util.Scanner;

public class FlightBookingSystem {

    static Connection con;
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {

        try {
            con = DriverManager.getConnection("jdbc:ucanaccess://db/flightbooking.accdb");
            System.out.println("Database connected successfully!");
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
            return;
        }

        int choice;
        do {
            System.out.println("\n===== FLIGHT BOOKING SYSTEM =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1) {
                register();
            } else if (choice == 2) {
                int userId = login();
                if (userId != -1) {
                    customerMenu(userId);
                }
            } else if (choice == 3) {
                System.out.println("Thank you! Goodbye.");
            } else {
                System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 3);
    }

    // ==========================================
    // CUSTOMER MENU
    // ==========================================
    static void customerMenu(int userId) {
        int choice;
        do {
            System.out.println("\n===== CUSTOMER MENU =====");
            System.out.println("1. Search Flights");
            System.out.println("2. Book a Flight");
            System.out.println("3. View My Bookings");
            System.out.println("4. Update Booking");
            System.out.println("5. Cancel Booking");
            System.out.println("6. View Booking Details");
            System.out.println("7. Logout");
            System.out.print("Enter choice: ");
            choice = sc.nextInt();
            sc.nextLine();

            if (choice == 1) {
                searchFlights();
            } else if (choice == 2) {
                bookFlight(userId);
            } else if (choice == 3) {
                viewMyBookings(userId);
            } else if (choice == 4) {
                updateBooking(userId);
            } else if (choice == 5) {
                cancelBooking(userId);
            } else if (choice == 6) {
                viewBookingDetails(userId);
            } else if (choice == 7) {
                System.out.println("Logged out successfully.");
            } else {
                System.out.println("Invalid choice. Try again.");
            }

        } while (choice != 7);
    }

    // ==========================================
    // REGISTER
    // ==========================================
    static void register() {
        System.out.println("\n--- REGISTER ---");
        System.out.print("Enter Full Name : ");
        String name = sc.nextLine();
        System.out.print("Enter Email     : ");
        String email = sc.nextLine();
        System.out.print("Enter Password  : ");
        String password = sc.nextLine();
        System.out.print("Enter Phone     : ");
        String phone = sc.nextLine();

        try {
            // Check if email already exists
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Users WHERE email = '" + email + "'");
            rs.next();
            int count = rs.getInt(1);

            if (count > 0) {
                System.out.println("Email already registered. Please login.");
                return;
            }

            // Insert new user
            String query = "INSERT INTO Users (full_name, email, password, phone) VALUES ('" + name + "', '" + email + "', '" + password + "', '" + phone + "')";
            stmt.executeUpdate(query);
            System.out.println("Registration successful! You can now login.");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // LOGIN
    // ==========================================
    static int login() {
        System.out.println("\n--- LOGIN ---");
        System.out.print("Enter Email    : ");
        String email = sc.nextLine();
        System.out.print("Enter Password : ");
        String password = sc.nextLine();

        try {
            Statement stmt = con.createStatement();

            // Step 1: Check if email exists
            ResultSet rs = stmt.executeQuery("SELECT * FROM Users WHERE email = '" + email + "'");

            if (!rs.next()) {
                System.out.println("No account found with this email. Please register first.");
                return -1;
            }

            // Step 2: Check password
            String dbPassword = rs.getString("password");
            if (!dbPassword.equals(password)) {
                System.out.println("Incorrect password. Please try again.");
                return -1;
            }

            // Step 3: Login success
            System.out.println("Welcome back, " + rs.getString("full_name") + "!");
            return rs.getInt("user_id");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return -1;
        }
    }

    // ==========================================
    // SEARCH FLIGHTS
    // ==========================================
    static void searchFlights() {
        System.out.println("\n--- SEARCH FLIGHTS ---");
        System.out.print("Enter Origin City      : ");
        String origin = sc.nextLine().toUpperCase();
        System.out.print("Enter Destination City : ");
        String destination = sc.nextLine().toUpperCase();

        try {
            Statement stmt = con.createStatement();

            // Join Flights with Airlines and Airports to get names
            String query = "SELECT f.flight_id, f.flight_number, a.airline_name, "
                         + "ap1.city AS from_city, ap2.city AS to_city, "
                         + "f.departure, f.arrival, f.available_seats "
                         + "FROM ((Flights f "
                         + "INNER JOIN Airlines a ON f.airline_id = a.airline_id) "
                         + "INNER JOIN Airports ap1 ON f.origin_id = ap1.airport_id) "
                         + "INNER JOIN Airports ap2 ON f.destination_id = ap2.airport_id "
                         + "WHERE UCASE(ap1.city) = '" + origin + "' "
                         + "AND UCASE(ap2.city) = '" + destination + "' "
                         + "AND f.available_seats > 0 "
                         + "AND f.fstatus = 'Scheduled'";

            ResultSet rs = stmt.executeQuery(query);

            System.out.println("\n------------------------------------------------------------");
            System.out.println("No.  FlightNo   Airline         From         To           Departure        Arrival          Seats");
            System.out.println("------------------------------------------------------------");

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(count + ".   "
                    + rs.getString("flight_number") + "     "
                    + rs.getString("airline_name") + "     "
                    + rs.getString("from_city") + "     "
                    + rs.getString("to_city") + "     "
                    + rs.getString("departure") + "     "
                    + rs.getString("arrival") + "     "
                    + rs.getInt("available_seats"));
            }

            if (count == 0) {
                System.out.println("No flights found for this route.");
            }
            System.out.println("------------------------------------------------------------");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // BOOK FLIGHT
    // ==========================================
    static void bookFlight(int userId) {
        System.out.println("\n--- BOOK FLIGHT ---");

        // Show flights first
        searchFlights();

        System.out.print("Enter Flight ID to book : ");
        int flightId = sc.nextInt();
        sc.nextLine();

        try {
            Statement stmt = con.createStatement();

            // Check flight exists and has seats
            ResultSet flightRs = stmt.executeQuery("SELECT * FROM Flights WHERE flight_id = " + flightId + " AND available_seats > 0 AND fstatus = 'Scheduled'");
            if (!flightRs.next()) {
                System.out.println("Flight not found or no seats available.");
                return;
            }

            // Show available classes for this flight
            System.out.println("\nAvailable Classes:");
            System.out.println("No.  Class       Price      Seats");
            System.out.println("----------------------------------");

            ResultSet classRs = stmt.executeQuery("SELECT * FROM FlightClasses WHERE flight_id = " + flightId + " AND avail_seats > 0");

            int classCount = 0;
            int[] classIds    = new int[10];
            double[] classPrices = new double[10];

            while (classRs.next()) {
                classCount++;
                classIds[classCount - 1]    = classRs.getInt("class_id");
                classPrices[classCount - 1] = classRs.getDouble("price");
                System.out.println(classCount + ".   "
                    + classRs.getString("class_name") + "     Rs."
                    + classRs.getDouble("price") + "     "
                    + classRs.getInt("avail_seats"));
            }

            if (classCount == 0) {
                System.out.println("No classes available.");
                return;
            }

            System.out.print("Select class number : ");
            int classChoice = sc.nextInt();
            sc.nextLine();

            if (classChoice < 1 || classChoice > classCount) {
                System.out.println("Invalid class selection.");
                return;
            }

            int classId       = classIds[classChoice - 1];
            double classPrice = classPrices[classChoice - 1];

            // Show available seats
            System.out.println("\nAvailable Seats:");
            ResultSet seatRs = stmt.executeQuery("SELECT seat_number FROM Seats WHERE flight_id = " + flightId + " AND class_id = " + classId + " AND is_booked = 'No'");
            int seatCount = 0;
            while (seatRs.next()) {
                System.out.print(seatRs.getString("seat_number") + "  ");
                seatCount++;
                if (seatCount % 10 == 0) {
                    System.out.println();
                }
            }
            System.out.println();

            System.out.print("\nEnter Number of Passengers : ");
            int numPass = sc.nextInt();
            sc.nextLine();

            // Passenger names and seats
            String[] passNames   = new String[numPass];
            int[]    passAges    = new int[numPass];
            String[] passGenders = new String[numPass];
            String[] passSeats   = new String[numPass];
            int[]    seatIds     = new int[numPass];

            for (int i = 0; i < numPass; i++) {
                System.out.println("\nPassenger " + (i + 1) + " Details:");
                System.out.print("Name       : ");
                passNames[i] = sc.nextLine();
                System.out.print("Age        : ");
                passAges[i] = sc.nextInt();
                sc.nextLine();
                System.out.print("Gender M/F : ");
                passGenders[i] = sc.nextLine();
                System.out.print("Seat No.   : ");
                passSeats[i] = sc.nextLine().toUpperCase();

                // Check seat is available
                ResultSet seatCheck = stmt.executeQuery("SELECT seat_id FROM Seats WHERE flight_id = " + flightId + " AND class_id = " + classId + " AND seat_number = '" + passSeats[i] + "' AND is_booked = 'No'");
                if (!seatCheck.next()) {
                    System.out.println("Seat " + passSeats[i] + " is not available. Please try again.");
                    return;
                }
                seatIds[i] = seatCheck.getInt("seat_id");
            }

            // Calculate total amount
            double totalAmount = classPrice * numPass;

            // Payment
            System.out.println("\nTotal Amount : Rs." + totalAmount);
            System.out.println("1. Card");
            System.out.println("2. UPI");
            System.out.println("3. Cash");
            System.out.print("Select Payment Method : ");
            int payChoice = sc.nextInt();
            sc.nextLine();

            String method = "";
            if (payChoice == 1) method = "Card";
            else if (payChoice == 2) method = "UPI";
            else method = "Cash";

            System.out.print("Confirm Booking? (yes/no) : ");
            String confirm = sc.nextLine();
            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Booking cancelled.");
                return;
            }

            // INSERT into Bookings
            stmt.executeUpdate("INSERT INTO Bookings (user_id, flight_id, booking_date, bstatus, total_amount) VALUES (" + userId + ", " + flightId + ", NOW(), 'Confirmed', " + totalAmount + ")");

            // Get the booking_id just inserted
            ResultSet bookRs = stmt.executeQuery("SELECT MAX(booking_id) FROM Bookings");
            bookRs.next();
            int bookingId = bookRs.getInt(1);

            // INSERT each passenger and update seat
            for (int i = 0; i < numPass; i++) {
                // INSERT Passenger
                stmt.executeUpdate("INSERT INTO Passengers (booking_id, class_id, seat_id, full_name, age, gender) VALUES (" + bookingId + ", " + classId + ", " + seatIds[i] + ", '" + passNames[i] + "', " + passAges[i] + ", '" + passGenders[i] + "')");

                // UPDATE Seat to booked
                stmt.executeUpdate("UPDATE Seats SET is_booked = 'Yes' WHERE seat_id = " + seatIds[i]);
            }

            // INSERT Payment
            stmt.executeUpdate("INSERT INTO Payments (booking_id, amount, method, pstatus, payment_type, transaction_date) VALUES (" + bookingId + ", " + totalAmount + ", '" + method + "', 'Paid', 'Booking Payment', NOW())");

            // UPDATE FlightClasses available seats
            stmt.executeUpdate("UPDATE FlightClasses SET avail_seats = avail_seats - " + numPass + " WHERE class_id = " + classId);

            // UPDATE Flights available seats
            stmt.executeUpdate("UPDATE Flights SET available_seats = available_seats - " + numPass + " WHERE flight_id = " + flightId);

            System.out.println("\nBooking Confirmed!");
            System.out.println("Booking ID   : " + bookingId);
            System.out.println("Passengers   : " + numPass);
            System.out.println("Total Paid   : Rs." + totalAmount);
            System.out.println("Payment Mode : " + method);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // VIEW MY BOOKINGS
    // ==========================================
    static void viewMyBookings(int userId) {
        System.out.println("\n--- MY BOOKINGS ---");

        try {
            Statement stmt = con.createStatement();

            String query = "SELECT b.booking_id, f.flight_number, a.airline_name, "
                         + "ap1.city AS from_city, ap2.city AS to_city, "
                         + "f.departure, b.bstatus, b.total_amount "
                         + "FROM (((Bookings b "
                         + "INNER JOIN Flights f ON b.flight_id = f.flight_id) "
                         + "INNER JOIN Airlines a ON f.airline_id = a.airline_id) "
                         + "INNER JOIN Airports ap1 ON f.origin_id = ap1.airport_id) "
                         + "INNER JOIN Airports ap2 ON f.destination_id = ap2.airport_id "
                         + "WHERE b.user_id = " + userId;

            ResultSet rs = stmt.executeQuery(query);

            System.out.println("------------------------------------------------------------");
            System.out.println("BookingID   FlightNo    Airline         From       To         Departure        Status       Amount");
            System.out.println("------------------------------------------------------------");

            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println(rs.getInt("booking_id") + "         "
                    + rs.getString("flight_number") + "       "
                    + rs.getString("airline_name") + "     "
                    + rs.getString("from_city") + "     "
                    + rs.getString("to_city") + "     "
                    + rs.getString("departure") + "     "
                    + rs.getString("bstatus") + "     "
                    + "Rs." + rs.getDouble("total_amount"));
            }

            if (count == 0) {
                System.out.println("No bookings found.");
            }
            System.out.println("------------------------------------------------------------");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // VIEW BOOKING DETAILS
    // ==========================================
    static void viewBookingDetails(int userId) {
        System.out.println("\n--- BOOKING DETAILS ---");
        System.out.print("Enter Booking ID : ");
        int bookingId = sc.nextInt();
        sc.nextLine();

        try {
            Statement stmt = con.createStatement();

            // Get booking + flight + airline + airport info
            String query = "SELECT b.booking_id, b.booking_date, b.bstatus, b.total_amount, "
                         + "f.flight_number, a.airline_name, "
                         + "ap1.city AS from_city, ap2.city AS to_city, "
                         + "f.departure, f.arrival "
                         + "FROM (((Bookings b "
                         + "INNER JOIN Flights f ON b.flight_id = f.flight_id) "
                         + "INNER JOIN Airlines a ON f.airline_id = a.airline_id) "
                         + "INNER JOIN Airports ap1 ON f.origin_id = ap1.airport_id) "
                         + "INNER JOIN Airports ap2 ON f.destination_id = ap2.airport_id "
                         + "WHERE b.booking_id = " + bookingId + " AND b.user_id = " + userId;

            ResultSet rs = stmt.executeQuery(query);

            if (!rs.next()) {
                System.out.println("Booking not found.");
                return;
            }

            System.out.println("\n-- Booking Info --");
            System.out.println("Booking ID  : " + rs.getInt("booking_id"));
            System.out.println("Flight      : " + rs.getString("flight_number") + " - " + rs.getString("airline_name"));
            System.out.println("From        : " + rs.getString("from_city"));
            System.out.println("To          : " + rs.getString("to_city"));
            System.out.println("Departure   : " + rs.getString("departure"));
            System.out.println("Arrival     : " + rs.getString("arrival"));
            System.out.println("Booked On   : " + rs.getString("booking_date"));
            System.out.println("Status      : " + rs.getString("bstatus"));
            System.out.println("Total Paid  : Rs." + rs.getDouble("total_amount"));

            // Get passenger details
            ResultSet pRs = stmt.executeQuery(
                "SELECT p.full_name, p.age, p.gender, fc.class_name, s.seat_number "
                + "FROM (Passengers p "
                + "INNER JOIN FlightClasses fc ON p.class_id = fc.class_id) "
                + "INNER JOIN Seats s ON p.seat_id = s.seat_id "
                + "WHERE p.booking_id = " + bookingId);

            System.out.println("\n-- Passengers --");
            System.out.println("No.  Name                 Age   Gender   Class       Seat");
            System.out.println("-----------------------------------------------------------");
            int i = 1;
            while (pRs.next()) {
                System.out.println(i + ".   "
                    + pRs.getString("full_name") + "     "
                    + pRs.getInt("age") + "     "
                    + pRs.getString("gender") + "     "
                    + pRs.getString("class_name") + "     "
                    + pRs.getString("seat_number"));
                i++;
            }

            // Get payment details
            ResultSet payRs = stmt.executeQuery("SELECT * FROM Payments WHERE booking_id = " + bookingId);
            if (payRs.next()) {
                System.out.println("\n-- Payment Info --");
                System.out.println("Method : " + payRs.getString("method"));
                System.out.println("Status : " + payRs.getString("pstatus"));
                System.out.println("Date   : " + payRs.getString("transaction_date"));
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // UPDATE BOOKING (passenger details only)
    // ==========================================
    static void updateBooking(int userId) {
        System.out.println("\n--- UPDATE BOOKING ---");
        System.out.print("Enter Booking ID : ");
        int bookingId = sc.nextInt();
        sc.nextLine();

        try {
            Statement stmt = con.createStatement();

            // Check booking belongs to user
            ResultSet bRs = stmt.executeQuery("SELECT * FROM Bookings WHERE booking_id = " + bookingId + " AND user_id = " + userId + " AND bstatus = 'Confirmed'");
            if (!bRs.next()) {
                System.out.println("Booking not found or already cancelled.");
                return;
            }

            // Show passengers
            ResultSet pRs = stmt.executeQuery("SELECT * FROM Passengers WHERE booking_id = " + bookingId);

            System.out.println("\nPassengers in this booking:");
            System.out.println("No.  PassengerID   Name                 Age   Gender");
            System.out.println("------------------------------------------------------");

            int count = 0;
            int[] passengerIds = new int[10];

            while (pRs.next()) {
                count++;
                passengerIds[count - 1] = pRs.getInt("passenger_id");
                System.out.println(count + ".   "
                    + pRs.getInt("passenger_id") + "          "
                    + pRs.getString("full_name") + "     "
                    + pRs.getInt("age") + "     "
                    + pRs.getString("gender"));
            }

            System.out.print("\nWhich passenger to update (number) : ");
            int pChoice = sc.nextInt();
            sc.nextLine();

            if (pChoice < 1 || pChoice > count) {
                System.out.println("Invalid selection.");
                return;
            }

            int passengerId = passengerIds[pChoice - 1];

            System.out.println("Press Enter to keep current value.");
            System.out.print("New Name   (or press Enter) : ");
            String newName = sc.nextLine();
            System.out.print("New Age    (or press Enter) : ");
            String newAge = sc.nextLine();
            System.out.print("New Gender (or press Enter) : ");
            String newGender = sc.nextLine();

            // Update only fields that were entered
            if (!newName.isEmpty()) {
                stmt.executeUpdate("UPDATE Passengers SET full_name = '" + newName + "' WHERE passenger_id = " + passengerId);
            }
            if (!newAge.isEmpty()) {
                stmt.executeUpdate("UPDATE Passengers SET age = " + Integer.parseInt(newAge) + " WHERE passenger_id = " + passengerId);
            }
            if (!newGender.isEmpty()) {
                stmt.executeUpdate("UPDATE Passengers SET gender = '" + newGender + "' WHERE passenger_id = " + passengerId);
            }

            System.out.println("Passenger details updated successfully.");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // CANCEL BOOKING
    // ==========================================
    static void cancelBooking(int userId) {
        System.out.println("\n--- CANCEL BOOKING ---");
        System.out.print("Enter Booking ID to cancel : ");
        int bookingId = sc.nextInt();
        sc.nextLine();

        try {
            Statement stmt = con.createStatement();

            // Check booking belongs to user
            ResultSet bRs = stmt.executeQuery("SELECT * FROM Bookings WHERE booking_id = " + bookingId + " AND user_id = " + userId + " AND bstatus = 'Confirmed'");
            if (!bRs.next()) {
                System.out.println("Booking not found or already cancelled.");
                return;
            }

            int flightId     = bRs.getInt("flight_id");
            double totalAmt  = bRs.getDouble("total_amount");

            System.out.println("Total Paid   : Rs." + totalAmt);
            System.out.println("Refund Amt   : Rs." + totalAmt);
            System.out.print("Enter reason for cancellation : ");
            String reason = sc.nextLine();
            System.out.print("Confirm cancellation? (yes/no) : ");
            String confirm = sc.nextLine();

            if (!confirm.equalsIgnoreCase("yes")) {
                System.out.println("Cancellation aborted.");
                return;
            }

            // Get all passengers of this booking
            ResultSet pRs = stmt.executeQuery("SELECT * FROM Passengers WHERE booking_id = " + bookingId);

            while (pRs.next()) {
                int seatId  = pRs.getInt("seat_id");
                int classId = pRs.getInt("class_id");

                // Free the seat
                stmt.executeUpdate("UPDATE Seats SET is_booked = 'No' WHERE seat_id = " + seatId);

                // Restore class seat count
                stmt.executeUpdate("UPDATE FlightClasses SET avail_seats = avail_seats + 1 WHERE class_id = " + classId);
            }

            // Count passengers to restore flight seats
            ResultSet countRs = stmt.executeQuery("SELECT COUNT(*) FROM Passengers WHERE booking_id = " + bookingId);
            countRs.next();
            int numPass = countRs.getInt(1);

            // Restore flight available seats
            stmt.executeUpdate("UPDATE Flights SET available_seats = available_seats + " + numPass + " WHERE flight_id = " + flightId);

            // Update booking status
            stmt.executeUpdate("UPDATE Bookings SET bstatus = 'Cancelled' WHERE booking_id = " + bookingId);

            // Update payment status
            stmt.executeUpdate("UPDATE Payments SET pstatus = 'Refunded' WHERE booking_id = " + bookingId);

            // Insert into Cancellations
            stmt.executeUpdate("INSERT INTO Cancellations (booking_id, cancelled_on, cancelled_by, reason, refund_amount, refund_status) VALUES (" + bookingId + ", NOW(), 'User', '" + reason + "', " + totalAmt + ", 'Pending')");

            System.out.println("Booking cancelled successfully.");
            System.out.println("Refund of Rs." + totalAmt + " will be processed.");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
