import java.time.Instant;
import java.util.*;

public class Scheduler {
    private final List<KernelandProcess> realTimeKernelandProcess;
    private final List<KernelandProcess> backgroundKernelandProcess;
    private final List<KernelandProcess> interactivePriorityKernelandProcess;
    private final List<Map.Entry<KernelandProcess, Instant>> sleepingProcess;
    private KernelandProcess currentKernelandProcess; // reference to KernelandProcess currently running
    private final Instant clock;

    /**
     * Constructs a scheduler which holds a list of processes, a timer instance, and a scheduled interrupt every 250ms
     */
    public Scheduler() {
        realTimeKernelandProcess = Collections.synchronizedList(new LinkedList<>());
        backgroundKernelandProcess = Collections.synchronizedList(new LinkedList<>());
        interactivePriorityKernelandProcess = Collections.synchronizedList(new LinkedList<>());
        sleepingProcess = Collections.synchronizedList(new LinkedList<>());
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
        if (currentKernelandProcess == null) SwitchProcess();
        return newProcess.getPid();
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
            System.out.println("Awake process is adding to respective queue");
            AppendKernelandProcess(awake.getPriority(), awake);
        }
        if (currentKernelandProcess != null) {
            System.out.println("Current process is not null");
            currentKernelandProcess.setTimeout(currentKernelandProcess.getTimeout() + 1);
            if (currentKernelandProcess.getTimeout() == 5) DemoteProcess(currentKernelandProcess);
            System.out.println("Stopping current process " + currentKernelandProcess);
            currentKernelandProcess.stop();
            System.out.println("Process is done: " + currentKernelandProcess.isDone());
            if (!currentKernelandProcess.isDone()) AppendKernelandProcess(currentKernelandProcess.getPriority(), currentKernelandProcess);
            System.out.println("Process list:" + interactivePriorityKernelandProcess);
        }
        RunRandomPriority();
    }
    /**
     * Chooses a random, valid priority value from the Priority enums
     * @return A random priority enum value; selection is based on probability model defined in assignment #2
     */
    private void RunRandomPriority() {
        double probability = Math.random();
        if (!realTimeKernelandProcess.isEmpty()) {
            if (probability <= 0.6) RunNextKernelandProcess(Priority.REALTIME);
            else if (probability <= 0.9 && !interactivePriorityKernelandProcess.isEmpty()) RunNextKernelandProcess(Priority.INTERACTIVE);
            else RunNextKernelandProcess(Priority.BACKGROUND);
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
        kp.setTimeout(0);
        switch (kp.getPriority()) {
            case REALTIME -> {
                System.out.println("Demoting from REALTIME to Interactive");
                kp.setPriority(Priority.INTERACTIVE);
                interactivePriorityKernelandProcess.add(kp);
            }
            case INTERACTIVE -> {
                System.out.println("Demoting from INTERACTIVE to BACKGROUND");
                kp.setPriority(Priority.BACKGROUND);
                backgroundKernelandProcess.add(kp);
            }
            default -> { }
        }
    }
    /**
     * Chooses the first item in the respective priority queue to run
     * @param priority A respective priority enum value
     */
    private void RunNextKernelandProcess(Priority priority) {
        switch (priority) {
            case REALTIME -> {
                if (!realTimeKernelandProcess.isEmpty()) {
                    KernelandProcess firstItem = realTimeKernelandProcess.remove(0); // first item
                    currentKernelandProcess = firstItem;
                    firstItem.run();
                }
            }
            case BACKGROUND -> {
                if (!backgroundKernelandProcess.isEmpty()) {
                    KernelandProcess firstItem = backgroundKernelandProcess.remove(0);
                    currentKernelandProcess = firstItem;
                    firstItem.run();
                }
            }
            case INTERACTIVE -> {
                if (!interactivePriorityKernelandProcess.isEmpty()) {
                    //System.out.println("Running interactive list");
                    KernelandProcess firstItem = interactivePriorityKernelandProcess.remove(0);
                    currentKernelandProcess = firstItem;
                    //System.out.println("First item in list: " + firstItem);
                    firstItem.run();
                }
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
    public void Sleep(int milliseconds) {
        // add to list of sleeping processes
        currentKernelandProcess.setTimeout(0);
        sleepingProcess.add(new AbstractMap.SimpleEntry<>(currentKernelandProcess, clock.plusMillis(milliseconds)));
        System.out.println("Putting process to sleep");
        var tmp = currentKernelandProcess;
        currentKernelandProcess = null;
        tmp.stop();
        //System.out.println("Switching process out of sleep");
        SwitchProcess();
    }
}
