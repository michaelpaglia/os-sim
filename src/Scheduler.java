import java.time.Instant;
import java.util.*;

public class Scheduler {
    private final List<KernelandProcess> realTimeKernelandProcess, backgroundKernelandProcess, interactivePriorityKernelandProcess;
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
        sleepingProcess = new LinkedList<>();
        Timer timer = new Timer();
        timer.schedule(new Interrupt(), 250, 250); // as per assignment requirements (#1)
        clock = Instant.now(); // current UTC time
    }

    /**
     * Constructs a KernelandProcess, adds it to the list of processes, and calls SwitchProcess() if not running
     * @param up A certain UserlandProcess to start
     * @return PID of new process
     */
    public synchronized int CreateProcess(UserlandProcess up) {
        return CreateProcess(up, Priority.INTERACTIVE);
    }

    /**
     * Constructs a KernelandProcess, adds it to the list of processes, and calls SwitchProcess() if not running
     * @param up A certain UserlandProcess to start
     * @param priority A certain priority to set to the UserlandProcess
     * @return PID of new process
     */
    public synchronized int CreateProcess(UserlandProcess up, Priority priority) {
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
    private synchronized void AppendKernelandProcess(Priority p, KernelandProcess kp) {
        switch (p) {
            case REALTIME -> realTimeKernelandProcess.add(kp);
            case BACKGROUND -> backgroundKernelandProcess.add(kp);
            case INTERACTIVE -> interactivePriorityKernelandProcess.add(kp);
        }
    }
    /**
     * If something is running, process stops; if process didn't finish, adds to list of processes
     */
    public synchronized void SwitchProcess() {
        // as long as there are sleeping items, check if the process' wake time is up and give chance to run
        synchronized (sleepingProcess) {
            while (!sleepingProcess.isEmpty() && sleepingProcess.get(0).getValue().isBefore(Instant.now())) { // get returns a tuple
                KernelandProcess awake = sleepingProcess.remove(0).getKey(); // remove from sleeping processes and choose KernelandProcess
                if (awake != null) {
                    awake.setTimeout(awake.getTimeout() + 1); // process ran to timeout, so we increment 1 to it's counter
                    // if process ran to timeout 5 times, we demote it
                    if (awake.getTimeout() == 5) DemoteProcess(awake);
                    awake.run();
                }
            }
        }
        if (currentKernelandProcess != null) { // process is running
            // put process onto correct queue
            if (!currentKernelandProcess.isDone()) AppendKernelandProcess(currentKernelandProcess.getPriority(), currentKernelandProcess);
            currentKernelandProcess.stop();
        }
        // choose a random queue to pick from and run next process from there
        // demote only if it has timed out 5 times
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
            if (probability < 0.6) return Priority.REALTIME;
            else if (probability < 0.3 && !interactivePriorityKernelandProcess.isEmpty()) return Priority.INTERACTIVE;
            else return Priority.BACKGROUND;
        } else if (!interactivePriorityKernelandProcess.isEmpty()) {
            // ¾ run interactive and ¼ run background
            double probability = Math.random();
            if (probability < 0.75) return Priority.INTERACTIVE;
            else return Priority.BACKGROUND;
        }
        return Priority.BACKGROUND;
    }

    /**
     * Demotes a process by one level if it ran to timeout 5 times
     * @param kp A certain KernelandProcess to demote down one priority
     */
    private synchronized void DemoteProcess(KernelandProcess kp) {
        switch (kp.getPriority()) {
            case REALTIME -> {
                kp.setPriority(Priority.INTERACTIVE);
                interactivePriorityKernelandProcess.add(kp);
            }
            case INTERACTIVE -> {
                kp.setPriority(Priority.BACKGROUND);
                backgroundKernelandProcess.add(kp);
            }
            default -> {}
        }
    }
    /**
     * Chooses the first item in the respective priority queue to run
     * @param priority A respective priority enum value
     */
    private synchronized void RunNextKernelandProcess(Priority priority) {
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
            default -> {}
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
        synchronized (sleepingProcess) { // add to list of sleeping processes
            sleepingProcess.add(new AbstractMap.SimpleEntry<>(currentKernelandProcess, clock.plusMillis(milliseconds)));
        }
        if (currentKernelandProcess != null) { // stop process from running in meantime
            var tmp = currentKernelandProcess;
            currentKernelandProcess = null;
            tmp.stop();
        }
        SwitchProcess(); // let process go to sleep
    }
}
