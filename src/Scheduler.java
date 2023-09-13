import java.time.Instant;
import java.util.*;

public class Scheduler {
    private final List<KernelandProcess> realTimeKernelandProcess, backgroundKernelandProcess, interactivePriorityKernelandProcess;
    private final List<Map.Entry<KernelandProcess, Instant>> sleepingProcess;
    private KernelandProcess currentKernelandProcess; // reference to KernelandProcess currently running
    private KernelandProcess process;
    private final Instant clock;

    /**
     * Constructs a scheduler which holds a list of processes, a timer instance, and a scheduled interrupt every 250ms
     */
    public Scheduler() {
        realTimeKernelandProcess = Collections.synchronizedList(new LinkedList<>());
        backgroundKernelandProcess = Collections.synchronizedList(new LinkedList<>());
        interactivePriorityKernelandProcess = Collections.synchronizedList(new LinkedList<>());
        sleepingProcess = new LinkedList<>();
        currentKernelandProcess = null;
        Timer timer = new Timer();
        timer.schedule(new Interrupt(), 250); // as per assignment requirements (#1)
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
        process = newProcess;
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
        if (currentKernelandProcess == null) currentKernelandProcess = process; // This probably isn't right
        // put process onto correct queue
        if (!currentKernelandProcess.isDone()) AppendKernelandProcess(currentKernelandProcess.getPriority(), currentKernelandProcess);
        currentKernelandProcess.stop();
        // choose a random queue to pick from and run next process from there
        RunNextKernelandProcess(ChooseRandomPriority());
    }

    /**
     * Chooses a random, valid priority value from the Priority enums
     * @return A random priority enum value; selection is based on probability model defined in assignment #2
     */
    private Priority ChooseRandomPriority() {
        if (!realTimeKernelandProcess.isEmpty()) {
            //  If there are real-time processes, then 6/10 we will run a real-time process, 3/10 we will run an interactive process (if there is one) otherwise 1/10 we will run a background process.
            double probability = Math.random();
            if (probability <= 0.6) return Priority.REALTIME;
            else if (probability <= 0.3 && !interactivePriorityKernelandProcess.isEmpty()) return Priority.INTERACTIVE;
            else return Priority.BACKGROUND;
        } else if (!interactivePriorityKernelandProcess.isEmpty()) {
            // ¾ run interactive and ¼ run background
            double probability = Math.random();
            if (probability <= 0.75) return Priority.INTERACTIVE;
            else return Priority.BACKGROUND;
        }
        return Priority.BACKGROUND;
    }

    /**
     * Demotes a process by one level if it ran to timeout 5 times
     * @param kp A certain KernelandProcess to demote down one priority
     */
    private void DemoteProcess(KernelandProcess kp) {
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
            default -> {break;}
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
                    firstItem.run();
                }}
            case BACKGROUND -> {
                if (!backgroundKernelandProcess.isEmpty()) {
                    KernelandProcess firstItem = backgroundKernelandProcess.remove(0);
                    firstItem.run();
                }}
            case INTERACTIVE -> {
                if (!interactivePriorityKernelandProcess.isEmpty()) {
                    KernelandProcess firstItem = interactivePriorityKernelandProcess.remove(0);
                    firstItem.run();
                }}
            default -> {break;}
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
        sleepingProcess.add(new AbstractMap.SimpleEntry<>(currentKernelandProcess, clock.plusMillis(milliseconds)));
        System.out.println("Added, switching process");
        SwitchProcess();

        // stop process from running in meantime
        var tmp = currentKernelandProcess;
        currentKernelandProcess = null;
        System.out.println("It's hanging here before stop");
        System.out.println("Process to stop " + tmp); // valid process
        tmp.stop();

        // as long as there are sleeping items, check if the process' wake time is up and give chance to run
        while (!sleepingProcess.isEmpty() && sleepingProcess.get(0).getValue().isBefore(Instant.now())) { // get returns a tuple
            KernelandProcess awake = sleepingProcess.remove(0).getKey(); // remove from sleeping processes and choose KernelandProcess
            awake.setTimeout(awake.getTimeout() + 1); // process ran to timeout, so we increment 1 to it's counter
            // if process ran to timeout 5 times, we demote it
            if (awake.getTimeout() == 5) DemoteProcess(awake);
            awake.run();
        }
    }

}
