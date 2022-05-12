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
        try {
            Thread.sleep((long) (Math.random() * 1000)); // employee commutes to restaurant
        } catch (InterruptedException e) {
        }
        Main.employeeLine.add(this); // adds employee to line
        while (Main.served < Main.numCustomers) {
            for (Table t : Main.tables) { // for each table check if there is no employee assigned to it if there isnt
                // assign one
                if (t.getAssignedEmployee() == null) {
                    assignEmployee();
                    break;
                }
            }
            DineIn_Employee emp = Main.employeeLine.remove(); // swap through available employees
            Main.employeeLine.add(emp);
            try {
                if (Main.dineLine.getQueueLength() > 0 && Main.seated < 12) {
                    assignCustomer();
                    // for (Table t : Main.tables) {
                    // System.out.println(
                    // t.customerSeated.toString() + " " + t.getName() + " " +
                    // t.customerSeated.size());
                    // }
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
                        if (t.getPaying() == t.customerSeated.size() && t.getAssignedEmployee() == this) {
                            for (Customer c : t.customerSeated) {
                                c.bill.release();
                                Main.served++;
                            }
                            t.setCustomerSeated();
                            t.setAvailable();
                            t.addSeats();
                            t.resetPaying();
                        }
                    }
                } else { // if customers are waiting to be seated then serve customers currently sitting
                    for (Table t : Main.tables) {
                        if (this.assignedTables.contains(t)) {
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
                        if (t.getPaying() == t.customerSeated.size() && t.getAssignedEmployee() == this) {
                            for (Customer c : t.customerSeated) {
                                c.bill.release();
                                Main.served++;
                            }
                            t.setCustomerSeated();
                            t.setAvailable();
                            t.addSeats();
                            t.resetPaying();
                        }
                    }
                    for (Table t : tables) {
                        if (t.getSeats() == 0) {
                            t.setNotAvailable();
                        } else if (t.getAvailable() == true && t.getAssignedEmployee() == this) { // if table is
                            // available
                            // and employee is
                            // assigned to it
                            if (Main.dineQueue.size() != 0) { // if there are customers waiting to be seated
                                assignCustomer(); // assign customer to table
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            Main.closeStore.acquire();
            Main.closeStore.release();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        msg("all customers have been served and employee is closing diner");
    }

    public void msg(String m) {
        System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m);
    }

    public void assignEmployee() { // assigns employee to table
        for (Table t : tables) {
            if (t.getAssignedEmployee() == null && Main.employeeLine.peek() == this) { // if table has no employee
                t.assignEmployee(Main.employeeLine.peek());
                msg("has been assigned to " + t.getName());
                try {
                    Thread.sleep((long) (Math.random() * 1000));

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assignedTables.add(t);
                break;
            }
        }
    }

    public void assignCustomer() { // assigns customer to table
        try {
            Main.seatingMutex.acquire();
            if (Main.dineLine.getQueueLength() >= 4) {
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
                        // System.out.println(table.getAvailable() + " " + table.getName());
                    }
                }
            } else { // if there are less than 4 customers in line
                for (Table table : assignedTables) {
                    if (table.getSeats() > 0 && table.getAssignedEmployee() == this) {
                        while (Main.dineQueue.size() > 0 && table.getSeats() > 0) {
                            Customer c = Main.dineQueue.remove(); // remove customer from line and assign them to table
                            table.addCustomer(c);
                            msg(c.getName() + " has been seated at " + table.getName());
                            Main.seated++;
                            Main.dineLine.release();
                        }
                    }
                }
            }
            Main.seatingMutex.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
