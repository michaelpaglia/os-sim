import java.time.Instant;
import java.util.*;

public class Scheduler {
    private final List<KernelandProcess> realTimeKernelandProcess, backgroundKernelandProcess, interactivePriorityKernelandProcess;
    private final List<Map.Entry<KernelandProcess, Instant>> sleepingProcess;
    private KernelandProcess currentKernelandProcess; // reference to KernelandProcess currently running
    private final Instant clock;
    private Kernel kernel;

    /**
     * Constructs a scheduler which holds a list of processes, a timer instance, and a scheduled interrupt every 250ms
     */
    public Scheduler() {
        realTimeKernelandProcess = Collections.synchronizedList(new LinkedList<>());
        backgroundKernelandProcess = Collections.synchronizedList(new LinkedList<>());
        interactivePriorityKernelandProcess = Collections.synchronizedList(new LinkedList<>());
        sleepingProcess = Collections.synchronizedList(new LinkedList<>());
        currentKernelandProcess = null;
        Timer timer = new Timer();
        timer.schedule(new Interrupt(), 250, 250); // as per assignment requirements (#1)
        clock = Instant.now(); // current UTC time
    }

    /**
     * Constructs a KernelandProcess, adds it to the list of processes, and calls SwitchProcess() if not running
     * @param up A certain UserlandProcess to start
     * @return PID of new process
     */
    public int CreateProcess(UserlandProcess up) {
        return CreateProcess(up, Priority.INTERACTIVE);
    }

    /**
     * Constructs a KernelandProcess, adds it to the list of processes, and calls SwitchProcess() if not running
     * @param up A certain UserlandProcess to start
     * @param priority A certain priority to set to the UserlandProcess
     * @return PID of new process
     */
    public int CreateProcess(UserlandProcess up, Priority priority) {
        KernelandProcess newProcess = new KernelandProcess(up, priority);
        AppendKernelandProcess(priority, newProcess);
        if (this.currentKernelandProcess == null) SwitchProcess();
        return newProcess.GetPID();
    }
    KernelandProcess GetCurrentProcess() {
        return this.currentKernelandProcess;
    }
    /**
     * Adds a process to a specific priority queue
     * @param p A respective priority enum value
     * @param kp A respective KernelandProcess
     */
    private void AppendKernelandProcess(Priority p, KernelandProcess kp) {
        switch (p) {
            case REALTIME -> realTimeKernelandProcess.add(kp);
            case BACKGROUND -> backgroundKernelandProcess.add(kp);
            case INTERACTIVE -> interactivePriorityKernelandProcess.add(kp);
        }
    }
    /**
     * If something is running, process stops; if process didn't finish, adds to list of processes
     */
    public void SwitchProcess() {
        // as long as there are sleeping items, check if the process' wake time is up and give chance to run
        while (!sleepingProcess.isEmpty() && sleepingProcess.get(0).getValue().isBefore(Instant.now())) { // get returns a tuple
            KernelandProcess awake = sleepingProcess.remove(0).getKey(); // remove from sleeping processes and choose KernelandProcess
            //this.currentKernelandProcess = awake;
            AppendKernelandProcess(awake.GetPriority(), awake);
        }
        if (this.currentKernelandProcess != null) {
            this.currentKernelandProcess.SetTimeout(this.currentKernelandProcess.GetTimeout() + 1);
            this.currentKernelandProcess.Stop();
            if (!this.currentKernelandProcess.IsDone()) {
                if (this.currentKernelandProcess.GetTimeout() == 5) DemoteProcess(this.currentKernelandProcess);
                else AppendKernelandProcess(this.currentKernelandProcess.GetPriority(), this.currentKernelandProcess);
            } else {
                // Close all of its open devices
                kernel.CloseAll();
            }
        }
        RunRandomPriority();
    }
    /**
     * Chooses a random, valid priority value from the Priority enums
     */
    private void RunRandomPriority() {
        double probability = Math.random();
        if (!realTimeKernelandProcess.isEmpty()) {
            if (probability <= 0.6) RunNextKernelandProcess(Priority.REALTIME);
            else if (probability <= 0.9 && !interactivePriorityKernelandProcess.isEmpty()) RunNextKernelandProcess(Priority.INTERACTIVE);
            else if (!backgroundKernelandProcess.isEmpty()) RunNextKernelandProcess(Priority.BACKGROUND);
        } else if (!interactivePriorityKernelandProcess.isEmpty()) {
            if (probability <= 0.75) RunNextKernelandProcess(Priority.INTERACTIVE);
            else if (!backgroundKernelandProcess.isEmpty()) RunNextKernelandProcess(Priority.BACKGROUND);
            else RunNextKernelandProcess(Priority.INTERACTIVE);
        } else if (!backgroundKernelandProcess.isEmpty()) {
            RunNextKernelandProcess(Priority.BACKGROUND);
        }
    }

    /**
     * Demotes a process by one level if it ran to timeout 5 times
     * @param kp A certain KernelandProcess to demote down one priority
     */
    private void DemoteProcess(KernelandProcess kp) {
        kp.SetTimeout(0);
        switch (kp.GetPriority()) {
            case REALTIME -> {
                System.out.println("Demoting from REALTIME to Interactive");
                kp.SetPriority(Priority.INTERACTIVE);
                interactivePriorityKernelandProcess.add(kp);
            }
            case INTERACTIVE -> {
                System.out.println("Demoting from INTERACTIVE to BACKGROUND");
                kp.SetPriority(Priority.BACKGROUND);
                backgroundKernelandProcess.add(kp);
            }
            default -> {}
        }
    }
    /**
     * Chooses the first item in the respective priority queue to run
     * @param priority A respective priority enum value
     */
    private void RunNextKernelandProcess(Priority priority) {
        switch (priority) {
            case REALTIME -> {
                KernelandProcess firstItem = realTimeKernelandProcess.remove(0); // first item
                this.currentKernelandProcess = firstItem;
                firstItem.Run();
            }
            case BACKGROUND -> {
                KernelandProcess firstItem = backgroundKernelandProcess.remove(0);
                this.currentKernelandProcess = firstItem;
                firstItem.Run();
            }
            case INTERACTIVE -> {
                //System.out.println("Running interactive list");
                KernelandProcess firstItem = interactivePriorityKernelandProcess.remove(0);
                this.currentKernelandProcess = firstItem;
                //System.out.println("First item in list: " + firstItem);
                firstItem.Run();
            }
            default -> { }
        }
    }

    /**
     * Used to schedule an interrupt in Scheduler constructor
     */
    private class Interrupt extends TimerTask {
        @Override
        public void run() { SwitchProcess(); }
    }

    /**
     * Adds the process to the list of sleeping processes based on time asleep, stops current process and switches processes
     * @param milliseconds Number of milliseconds to sleep
     */
    public synchronized void Sleep(int milliseconds) {
        // add to list of sleeping processes
        this.currentKernelandProcess.SetTimeout(0);
        sleepingProcess.add(new AbstractMap.SimpleEntry<>(this.currentKernelandProcess, clock.plusMillis(milliseconds)));
        sleepingProcess.sort(Map.Entry.comparingByValue()); // sort the list in the order of wake-up time
        var tmp = this.currentKernelandProcess;
        //System.out.println(tmp);
        this.currentKernelandProcess = null;
        tmp.Stop();
        SwitchProcess();
    }
}
