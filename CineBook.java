
import java.util.*;

public class CineBook {
    static Scanner sc = new Scanner(System.in);
    static ArrayList<Movie> movies = new ArrayList<>();
    static ArrayList<Booking> bookings = new ArrayList<>();
    static HashMap<String, Integer> coupons = new HashMap<>();
    static boolean[][] seats = new boolean[4][10];
    static SnackQueue snackQueue = new SnackQueue();
    static int nextId = 1001;
    static String[] foods = {"Popcorn", "Coke", "Nachos"};
    static int[] foodPrices = {120, 80, 150};

    // Abstraction
    static abstract class Ticket {
        abstract int price();
    }

    // Movie class
    static class Movie extends Ticket {
        private String name, genre, time;
        private int rate;

        Movie(String n, String g, String t, int p) {
            name = n; genre = g; time = t; rate = p;
        }

        int price() { return rate; }

        public String toString() {
            return name + " | " + genre + " | " + time + " | Rs." + rate;
        }
    }

    // Booking class
    static class Booking {
        private int id, count, snackBill, discount;
        private String customer;
        private Movie movie;

        Booking(int i, String c, Movie m, int n) {
            id = i; customer = c; movie = m; count = n;
        }

        int total() {
            return movie.price() * count + snackBill - discount;
        }

        void show() {
            System.out.println(id + " | " + customer + " | " + movie.name
                + " | Tickets: " + count + " | Total: Rs." + total());
        }
    }

    // Inheritance and polymorphism
    static class PremiumBooking extends Booking {
        PremiumBooking(int i, String c, Movie m, int n) {
            super(i, c, m, n);
        }

        @Override
        int total() {
            return super.total() + 100;
        }
    }

    // Custom exceptions
    static class MovieNotFoundException extends Exception {
        MovieNotFoundException() {
            super("Movie not found!");
        }
    }

    static class BookingNotFoundException extends Exception {
        BookingNotFoundException() {
            super("Booking not found!");
        }
    }

    // Snack order class
    static class SnackOrder {
        int bookingId, quantity;
        String item;

        SnackOrder(int id, String s, int q) {
            bookingId = id; item = s; quantity = q;
        }
    }

    // Custom Queue using Linked Nodes (DSA)
    static class SnackQueue {
        static class Node {
            SnackOrder data;
            Node next;

            Node(SnackOrder d) { data = d; }
        }

        Node front, rear;

        // Insert at rear
        void enqueue(SnackOrder s) {
            Node n = new Node(s);
            if (rear == null) front = n;
            else rear.next = n;
            rear = n;
        }

        // Remove from front (FIFO)
        SnackOrder dequeue() {
            if (front == null) return null;
            SnackOrder s = front.data;
            front = front.next;
            if (front == null) rear = null;
            return s;
        }

        // Remove orders of cancelled booking
        void removeBooking(int id) {
            SnackQueue remaining = new SnackQueue();
            SnackOrder s;
            while ((s = dequeue()) != null)
                if (s.bookingId != id) remaining.enqueue(s);
            front = remaining.front;
            rear = remaining.rear;
        }
    }

    // Safe integer input
    static int number(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    // Add sample movies
    static void addSampleMovies() {
        movies.add(new Movie("Avatar", "Sci-Fi", "Morning", 200));
        movies.add(new Movie("Inception", "Thriller", "Evening", 180));
        movies.add(new Movie("Interstellar", "Sci-Fi", "Afternoon", 250));
        coupons.put("STUDENT10", 10);
        coupons.put("FEST20", 20);
    }

    // Find movie
    static Movie findMovie(int index) throws MovieNotFoundException {
        if (index < 1 || index > movies.size())
            throw new MovieNotFoundException();
        return movies.get(index - 1);
    }

    // Find booking
    static Booking findBooking(int id) throws BookingNotFoundException {
        for (Booking b : bookings)
            if (b.id == id) return b;
        throw new BookingNotFoundException();
    }

    // View movies
    static void displayMovies() {
        for (int i = 0; i < movies.size(); i++)
            System.out.println((i + 1) + ". " + movies.get(i));
    }

    // Book tickets
    static void bookTickets() throws MovieNotFoundException {
        displayMovies();
        Movie m = findMovie(number("Movie number: "));
        System.out.print("Customer name: ");
        String name = sc.nextLine();
        int count = number("Number of tickets: ");

        if (count <= 0) {
            System.out.println("Invalid quantity.");
            return;
        }

        int type = number("1. Regular  2. Premium (+Rs.100): ");
        Booking b = type == 2
            ? new PremiumBooking(nextId++, name, m, count)
            : new Booking(nextId++, name, m, count);

        bookings.add(b);
        System.out.println("Booked! ID: " + b.id);
    }

    // View bookings
    static void viewBookings() {
        if (bookings.isEmpty()) System.out.println("No bookings.");
        for (Booking b : bookings) b.show();
    }

    // Cancel booking
    static void cancelBooking() throws BookingNotFoundException {
        Booking b = findBooking(number("Booking ID: "));
        bookings.remove(b);
        snackQueue.removeBooking(b.id);
        System.out.println("Booking cancelled.");
    }

    // Calculate bill
    static void calculateBill() throws BookingNotFoundException {
        Booking b = findBooking(number("Booking ID: "));
        System.out.println("Final bill: Rs." + b.total());
    }

    // Movie and showtime management
    static void movieManagement() {
        int ch = number("1.Add movie  2.Remove movie  3.Show timings: ");

        if (ch == 3) {
            displayMovies();
            return;
        }

        if (ch == 2) {
            displayMovies();
            int i = number("Remove movie number: ");
            if (i > 0 && i <= movies.size())
                movies.remove(i - 1);
            else System.out.println("Invalid movie.");
        } else if (ch == 1) {
            System.out.print("Name: ");
            String n = sc.nextLine();
            System.out.print("Genre: ");
            String g = sc.nextLine();
            System.out.print("Showtime: ");
            String t = sc.nextLine();
            int p = number("Price: ");
            if (p > 0) movies.add(new Movie(n, g, t, p));
        }
    }

    // Member 2: Snacks and food ordering
    

static void orderSnacks() throws BookingNotFoundException {

    Booking b = findBooking(number("Booking ID: "));

    ArrayList<SnackOrder> currentOrder = new ArrayList<>();
    int sessionTotal = 0;

    while (true) {

        System.out.println("\n=== AVAILABLE SNACKS ===");

        
System.out.println("\n========== SNACK MENU ==========");
System.out.printf("%-5s %-15s %s%n", "No.", "Snack", "Price");
System.out.println("--------------------------------");

for (int i = 0; i < foods.length; i++) {
    System.out.printf("%-5d %-15s Rs.%d%n",
            i + 1, foods[i], foodPrices[i]);
}

System.out.println("--------------------------------");
System.out.println("0. Finish ordering");


        System.out.println("0. Finish ordering");

        int choice = number("Select snack: ");

        if (choice == 0) {
            break;
        }

        int index = choice - 1;

        if (index < 0 || index >= foods.length) {
            System.out.println("Invalid snack selection.");
            continue;
        }

        int quantity = number("Enter quantity: ");

        if (quantity <= 0) {
            System.out.println("Quantity must be positive.");
            continue;
        }

        SnackOrder order = new SnackOrder(
                b.id, foods[index], quantity);

        currentOrder.add(order);
        snackQueue.enqueue(order);

        int itemTotal = foodPrices[index] * quantity;

        b.snackBill += itemTotal;
        sessionTotal += itemTotal;

        
System.out.println("\nSnack added successfully!");
System.out.println("Selected Item: " + foods[index]);
System.out.println("Quantity: " + quantity);
System.out.println("Price per item: Rs." + foodPrices[index]);
System.out.println("Total Price: Rs." + itemTotal);
System.out.println("-----------------------------");

    }

    System.out.println("\n=== SNACK ORDER SUMMARY ===");

    if (currentOrder.isEmpty()) {
        System.out.println("No snacks ordered.");
    }

    
for (int i = 0; i < foods.length; i++) {

    int totalQuantity = 0;

    for (SnackOrder order : currentOrder) {
        if (order.item.equals(foods[i])) {
            totalQuantity += order.quantity;
        }
    }

    if (totalQuantity > 0) {
        int itemTotal = foodPrices[i] * totalQuantity;

        System.out.println(foods[i] + " x "
                + totalQuantity + " = Rs." + itemTotal);
    }
}


    System.out.println("-------------------------");
    System.out.println("Session Total: Rs." + sessionTotal);
    System.out.println("Booking Snack Bill: Rs." + b.snackBill);
}


// Process snack orders using FIFO
static void processSnack() {

    SnackOrder s = snackQueue.dequeue();

    if (s == null) {
        System.out.println("No pending snack orders.");
    } else {
        System.out.println("Snack order served successfully!");
        System.out.println("Item: " + s.item);
        System.out.println("Quantity: " + s.quantity);
        System.out.println("Booking ID: " + s.bookingId);
    }
}

    // Discounts and offers
    static void applyDiscount() throws BookingNotFoundException {
        Booking b = findBooking(number("Booking ID: "));
        System.out.print("Coupon (STUDENT10/FEST20): ");
        String code = sc.nextLine().toUpperCase();

        Integer percent = coupons.get(code);

        if (percent == null) {
            System.out.println("Invalid coupon.");
            return;
        }

        b.discount = b.movie.price() * b.count * percent / 100;
        System.out.println("Discount applied. Total: Rs." + b.total());
    }

    // Seat selection
    static void selectSeat() throws BookingNotFoundException {
        Booking b = findBooking(number("Booking ID: "));

        for (int r = 0; r < 4; r++) {
            for (int c = 0; c < 10; c++)
                System.out.print(seats[r][c] ? "X " : "O ");
            System.out.println();
        }

        int r = number("Row (1-4): ") - 1;
        int c = number("Seat (1-10): ") - 1;

        if (r < 0 || r >= 4 || c < 0 || c >= 10 || seats[r][c]) {
            System.out.println("Seat unavailable.");
            return;
        }

        seats[r][c] = true;
        System.out.println("Seat selected for booking " + b.id);
    }

    // Main menu
    public static void main(String[] args) {
        addSampleMovies();

        while (true) {
            System.out.println("\n=== CINEBOOK ===");
            System.out.println("1.View Movies  2.Book Tickets");
            System.out.println("3.View Bookings  4.Cancel Booking");
            System.out.println("5.Calculate Bill  6.Movie Management");
            System.out.println("7.Order Snacks  8.Process Snacks");
            System.out.println("9.Apply Discount  10.Select Seat");
            System.out.println("11.Exit");

            int ch = number("Choice: ");

            try {
                switch (ch) {
                    case 1: displayMovies(); break;
                    case 2: bookTickets(); break;
                    case 3: viewBookings(); break;
                    case 4: cancelBooking(); break;
                    case 5: calculateBill(); break;
                    case 6: movieManagement(); break;
                    case 7: orderSnacks(); break;
                    case 8: processSnack(); break;
                    case 9: applyDiscount(); break;
                    case 10: selectSeat(); break;
                    case 11:
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            } catch (MovieNotFoundException | BookingNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
