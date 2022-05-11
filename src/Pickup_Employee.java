
public class Pickup_Employee extends Thread implements Runnable {
    private long time;

    public Pickup_Employee(String id, long time) {
        setName("Pickup_Employee " + id);
        this.time = time;
    }

    @Override
    public void run() {
        while (Main.served < Main.numCustomers || Main.customers[0].isAlive()) {
            if (Main.pickUpLine.getQueueLength() > 0) {
                try {
                    msg("took the customers order and is preparing the order");

                    Thread.sleep((long) (Math.random() * 1000)); // prepares order

                    Main.pickUpLine.release();
                    msg("has finished preparing the order and is waiting for customer to pay");
                    Main.pickUpPay.acquire(); // waits for customer to pay
                    Main.pickUpMutex.acquire();
                    Main.served++;
                    Main.pickUpMutex.release();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        msg("is done serving customers");
    }

    public void msg(String m) {
        System.out.println("[" + (System.currentTimeMillis() - time) + "] " + getName() + ": " + m);
    }
}
