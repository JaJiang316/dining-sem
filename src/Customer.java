import java.util.concurrent.Semaphore;

public class Customer extends Thread implements Runnable {
    private long time;
    public Semaphore ordering = new Semaphore(0);
    public Semaphore waitFood = new Semaphore(0);
    public Semaphore bill = new Semaphore(0);
    public boolean orderingTime = false;

    public Customer(String id, long time) { // constructor
        setName("Customer " + id);
        this.time = time;
    }

    @Override
    public void run() {
        int order = getOrder();
        try {
            Thread.sleep((long) (Math.random() * 1000));
            msg("is commuting to diner");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (order <= 2) { // pickup
            msg("has decided to pick up and got on the pickup line.");
            getOnLine(); // customer gets on line
            try {
                msg("is paying for their order");
                Thread.sleep((long) (Math.random() * 1000)); // is paying for their order
                Main.pickUpPay.release();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else if (order > 2) { // dine-in
            msg("has decided to dine-in and got on the line.");
            Main.dineQueue.add(this);
            try {
                Main.dineLine.acquire(); // customer gets on line
                msg("has been seated and is checking the menu and placing order");
                Thread.sleep((long) (Math.random() * 1000)); // checks menu and places order
                orderingTime = true;
                ordering.acquire(); // waits until the employee takes their order
                waitFood.acquire(); // waits for the food to be served
                orderingTime = false;
                msg("has been served and is eating their food");
                Thread.sleep((long) (Math.random() * 1000)); // eats food
                Main.doneEat.acquire();
                Main.done++;
                for (Table t : Main.tables) {
                    if (t.customerSeated.contains(this)) {
                        t.setPaying();
                        // System.out.println(t.getAvailable() + " " + t.getName() + " " +
                        // t.getPaying());
                    }
                }
                Main.doneEat.release();
                msg("is done eating and is waiting for their bill");
                bill.acquire(); // waits for the employee to take tables bill
                msg("is paying their bill");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        msg("has been served and is leaving the diner.");
        try {
            Thread.sleep((long) (Math.random() * 1000)); // leaves diner
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (Main.served == Main.numCustomers) {
            Main.closeStore.release(); // release the employees to close the store
        }
    }

    public void msg(String m) {
        System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m);
    }

    public int getOrder() { // get order
        return (int) Math.floor(Math.random() * 10 + 1);
    }

    public void getOnLine() { // get on line
        try {
            Main.pickUpLine.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getOnDineLine() { // get on line
        try {
            Main.dineLine.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean isOrdering() {
        return orderingTime;
    }
}
