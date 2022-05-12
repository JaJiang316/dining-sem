import java.util.ArrayList;

public class Table {

    private String id;
    private int seats = Main.numSeats;
    private DineIn_Employee assignedEmployee = null;
    private boolean assigning = true;
    public ArrayList<Customer> customerSeated = new ArrayList<>();
    public int paying = 0;
    private static boolean available = true;
    private long time;

    public Table(String id, long time) { // constructor
        setName("Table " + id);
        this.time = time;
    }

    public void setName(String string) { // sets table id
        this.id = string;
    }

    public String getName() { // returns table id
        return id;
    }

    public void addCustomer(Customer c) { // adds customer to table
        if (seats > 0) {
            seats--;
            customerSeated.add(c);
        }
        if (seats == 0) { // if no more seats available set table as not available
            setNotAvailable();
        }
    }

    public void assignEmployee(DineIn_Employee e) { // asigns employeee to table and make it so no more employees can be
                                                    // assigned to the table
        while (assigning) {
            assigning = false;
            if (assignedEmployee == null) {
                msg("Assigning " + e.getName() + " to " + this.getName());
                assignedEmployee = e;
            } else {
                msg("Employee " + e.getName() + " is already assigned to " + this.getName());
            }
        }
    }

    public Object getAssignedEmployee() { // return assigned employee
        return assignedEmployee;
    }

    public int getSeats() { // returns number of seats
        return seats;
    }

    public void addSeats() { // adds seats to table
        seats = 4;
    }

    public void setSeats() { // sets number of seats
        this.seats = seats - 1;
    }

    public void setNotAvailable() { // sets table as not available
        available = false;
    }

    public void setAvailable() { // sets table as available
        available = true;
    }

    public boolean getAvailable() { // returns if table is available
        return available;
    }

    public void setPaying() { // sets number of paying customers
        paying++;
    }

    public int getPaying() { // returns number of paying customers
        return paying;
    }

    public void msg(String m) {
        System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m);
    }

    public void setCustomerSeated() { // sets customer seated
        customerSeated = new ArrayList<>();
    }

    public void resetPaying() { // resets number of paying customers
        paying = 0;
    }
}