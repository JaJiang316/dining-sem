import java.util.ArrayList;

public class DineIn_Employee extends Thread implements Runnable {
    private long time;
    private Table[] tables = new Table[Main.numTables];
    private ArrayList<Table> assignedTables = new ArrayList<>();

    public DineIn_Employee(String id, long time, Table[] tables) { // constructor
        setName("Dinein_Employee " + id);
        this.time = time;
        this.tables = tables;
    }

    @Override
    public void run() {
        Main.employeeLine.add(this); // adds employee to line
        // try {
        // Thread.sleep((long) (Math.random() * 1000)); // employee commutes to
        // restaurant
        // } catch (InterruptedException e) {
        // }
        while (Main.served < Main.numCustomers || Main.pickup_employee.isAlive()) {
            for (Table t : tables) { // for each table check if there is no employee assigned to it if there isnt
                                     // assign one
                // try {
                if (t.getAssignedEmployee() == null) {
                    assignEmployee();
                    break;
                }
                // } catch (InterruptedException e) {
                // e.printStackTrace();
                // }
            }
            DineIn_Employee emp = Main.employeeLine.remove(); // swap through available employees
            Main.employeeLine.add(emp);
            try {
                if (Main.dineLine.getQueueLength() > 0 && Main.seated < 12) {
                    assignCustomer();
                } else if (Main.dineQueue.isEmpty() && Main.employeeLine.peek() == this) { // if everybody has been
                                                                                           // seated
                    for (Table t : tables) { // for each table that the employee is assigned to check if a customer is
                        if (this.assignedTables.contains(t)) { // ordering and take their order
                            for (Customer c : t.customerSeated) {
                                if (c.isOrdering() && t.getAssignedEmployee() == this) {
                                    msg("is taking customer " + c.getName() + " order");
                                    c.ordering.release();
                                    Thread.sleep((long) (Math.random() * 1000)); // prepares order
                                    c.waitFood.release();
                                }
                            }
                        }
                    }
                    for (Table t : tables) {
                        if (t.getPaying() == 4 && t.getAssignedEmployee() == this) {
                            for (Customer c : t.customerSeated) {
                                c.bill.release();
                                Main.served++;
                            }
                            t.setCustomerSeated();
                            t.setAvailable();
                            t.addSeats(4);
                            t.resetPaying();
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        msg("all customers have been served");
    }

    public void msg(String m) {
        System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m);
    }

    public void assignEmployee() { // assigns employee to table
        // msg("is getting a table");
        for (Table t : tables) {
            if (t.getAssignedEmployee() == null && Main.employeeLine.peek() == this) { // if table has no employee
                // assigned to it assign it
                // to
                // employee
                t.assignEmployee(this);
                DineIn_Employee emp = Main.employeeLine.remove(); // swap through available employees
                Main.employeeLine.add(emp);
                assignedTables.add(t);
                break;
            }
        }
    }

    public void assignCustomer() { // assigns customer to table
        if (Main.dineLine.getQueueLength() >= 4) {
            try {
                Main.seatingMutex.acquire();
                for (Table table : assignedTables) {
                    if (table.getSeats() > 0 && table.getAssignedEmployee() != null && Main.dineQueue.size() >= 4) {
                        for (int i = table.getSeats(); i > 0; i--) { // for each seat in table remove customer from line
                                                                     // and
                                                                     // add them to table
                            Customer c = Main.dineQueue.remove();
                            table.addCustomer(c);
                            Main.seated++;
                            msg(c.getName() + " has been seated at " + table.getName());
                            Main.dineLine.release();
                        }
                        // table.setNotAvailable(); // set table to not available
                        // System.out.println(table.getAvailable() + " " + table.getName());
                    }
                }
                Main.seatingMutex.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else { // if there are less than 4 customers in line
            try {
                Main.seatingMutex.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (Table table : assignedTables) {
                while (Main.employeeLine.peek() != this) { // while employee is not at front of line make him in front
                    // of line
                    DineIn_Employee emp = Main.employeeLine.remove();
                    Main.employeeLine.add(emp);
                }
                // if table has seats and employee is assigned to it and other employee isnt
                // assigning customers to tables
                if (table.getSeats() > 0 && table.getAssignedEmployee() == this) {
                    while (Main.dineQueue.size() > 0) {
                        Customer c = Main.dineQueue.remove(); // remove customer from line and assign them to table
                        table.addCustomer(c);
                        msg(c.getName() + " has been seated at " + table.getName());
                        Main.seated++;
                        Main.dineLine.release();
                    }
                    table.setNotAvailable();
                    for (int i = 0; i < Main.dineQueue.size(); i++) {
                        Customer c = Main.dineQueue.remove(); // remove customer from line and assign them to table
                        table.addCustomer(c);
                        msg(c.getName() + " has been seated at " + table.getName());
                        Main.seated++;
                        Main.dineLine.release();
                    }
                    table.setNotAvailable(); // set table to not available
                }
            }
            Main.seatingMutex.release();
        }

    }
}
