/*
 Tongbo Sui
 Oct. 10th, 2012
 The program implements the Sleeping Barber problem.
*/

import java.util.*;
import java.lang.*;

class Init {    //master class
    private static final int chair = 3;    //chairs for waiting customers
    private static final int bar = 1;    //how many barbers do we have
    public static final int n = 10;    //how many would be here today
    public static int cut = 0;    //how many have been cut
    public static Sema customers = new Sema(chair);    //semaphore for chairs; how many are occupied
    public static Sema barbers = new Sema(bar);    //semaphore for barbers; how many are occupied

    public static void main(String[] args) {
        Thread b = new Thread(new Barber());    //barber thread
        Thread[] cus = new Thread[n];    //customer threads
        b.start();
        Random r = new Random();
        int ri = r.nextInt(1000);
        System.out.println("*** Shop opened ***");
        for (int i = 0; i < n; i++) {
            ri = r.nextInt(1000);    //customer comes in at a random delay
            try {
                Thread.sleep(ri);
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
            cus[i] = new Thread(new Customer());
            cus[i].start();
        }
        for (int i = 0; i < n; i++) {    //wait until all customers gone
            try {
                cus[i].join();
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
        }
        try {    //wait until barbers sleep after all customers
            b.join();
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        }
    }
}

class Barber implements Runnable {    //barber class
    public void run() {
        barber();
    }

    private synchronized void cutHair() {    //sleep for a time, cut hair
        System.out.println("Cutting hair...");
        try {
            Thread.sleep(800);    //sleep
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        }
        Init.cut++;
        System.out.println("Hair cut...");
    }

    private synchronized void barber() {
        while (true) {
            if (Init.cut == Init.n) {    //all customers are either cut or gone
                System.out.println("*** Let's call it a day ***");
                return;
            }
            System.out.println("Sleeping...");
            while (Init.barbers.val() == 0) {    //while no one is acquiring the barber
                try {
                    wait(10);    //sleep
                } catch (InterruptedException e) {
                    System.out.println(e.toString());
                }
            }
            //when someone wakes the barber
            cutHair();    //cut his hair
            try {
                Init.barbers.down();    //try clear occupied barber counter
            } catch (InterruptedException e) {
                System.out.println(e.toString());
            }
            notify();
            try {
                Init.customers.down();    //try clear waiting counter
            } catch (InterruptedException a) {    //when customers are cleared
                try {
                    wait(10);    //back to sleep
                } catch (InterruptedException e) {
                    System.out.println(e.toString());
                }
            }
        }
    }
}

class Customer implements Runnable {    //customer class
    public void run() {
        customer();
    }

    private synchronized void customer() {
        System.out.println("New customer has arrived...");
        try {
            Init.barbers.up();    //try wake up the barber
            notifyAll();
            System.out.println("\tWoke up barber...");
        } catch (InterruptedException a) {    //if failed
            try {
                Init.customers.up();    //try to sit in a chair
                System.out.println("\tNew customer waiting in chair...");
                while (Init.barbers.val() == 1) {    //while the barber is occupied
                    try {
                        wait(10);    //wait
                    } catch (InterruptedException e) {
                        System.out.println(e.toString());
                    }
                }
                try {
                    Init.barbers.up();    //try wake up the barber
                    notifyAll();
                    System.out.println("\tWoke up barber...");
                } catch (InterruptedException e) {
                    System.out.println(e.toString());
                }
                return;    //not occupied; had hair cut
            } catch (InterruptedException b) {    //if all efforts failed, leave
                System.out.println("\tChair full. Customer left...");
                Init.cut++;
                return;
            }
        }
    }
}

class Sema {    //semaphore class
    public Sema(int bound) {    //constructor
        upperBound = bound;
        value = 0;
    }

    public synchronized void up() throws InterruptedException {
        if (value == upperBound) {
            throw new InterruptedException();
        } else {
            value++;
        }
    }

    public synchronized void down() throws InterruptedException {
        if (value == 0) {
            throw new InterruptedException();
        } else {
            value--;
        }
    }

    public synchronized int val() {
        return value;
    }

    private volatile int value;    //the actual semaphore value
    private int upperBound;    //the semaphore should not have a value larger than this
}