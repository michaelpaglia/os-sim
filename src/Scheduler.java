import java.time.Instant;
import java.util.*;

public class Scheduler {
    private LinkedList<KernelandProcess> highPriorityKernelandProcess;
    private LinkedList<KernelandProcess> mediumPriorityKernelandProcess;
    private LinkedList<KernelandProcess> lowPriorityKernelandProcess;
    private LinkedList<KernelandProcess> interactivePriorityKernelandProcess;
    private PriorityQueue<Map.Entry<KernelandProcess, Instant>> sleepingProcessList;
    Timer timer;
    KernelandProcess currentKernelandProcess; // reference to KernelandProcess currently running
    private Instant clock;

    /**
     * Constructs a scheduler which holds a list of processes, a timer instance, and a scheduled interrupt every 250ms
     */
    public Scheduler() {
        highPriorityKernelandProcess = new LinkedList<>();
        mediumPriorityKernelandProcess = new LinkedList<>();
        lowPriorityKernelandProcess = new LinkedList<>();
        interactivePriorityKernelandProcess = new LinkedList<>();
        sleepingProcessList = new PriorityQueue<>(Map.Entry.comparingByValue());
        timer = new Timer();
        timer.schedule(new Interrupt(), 250);
        clock = Instant.now();
    }

    /**
     * Constructs a KernelandProcess, adds it to the list of processes, and calls SwitchProcess() if not running
     * @param up A certain UserlandProcess to start
     * @return PID of new process
     */
    public synchronized int CreateProcess(UserlandProcess up) {
        KernelandProcess newProcess = new KernelandProcess(up);
        interactivePriorityKernelandProcess.add(newProcess);
        if (currentKernelandProcess == null) SwitchProcess();
        return newProcess.getPid();
    }

    /**
     * Constructs a KernelandProcess, adds it to the list of processes, and calls SwitchProcess() if not running
     * @param up A certain UserlandProcess to start
     * @param priority A certain priority to set to the UserlandProcess
     * @return PID of new process
     */
    public synchronized int CreateProcess(UserlandProcess up, Priority priority) {
        KernelandProcess newProcess = new KernelandProcess(up, priority);
        addToRespectivePriorityQueue(priority, newProcess);
        if (currentKernelandProcess == null) SwitchProcess();
        return newProcess.getPid();
    }

    /**
     * Adds a process to a specific priority queue
     * @param priority A respective priority enum value
     * @param newProcess A respective KernelandProcess
     */
    private void addToRespectivePriorityQueue(Priority priority, KernelandProcess newProcess) {
        switch (priority) {
            case HIGH -> highPriorityKernelandProcess.add(newProcess);
            case MEDIUM -> mediumPriorityKernelandProcess.add(newProcess);
            case LOW -> lowPriorityKernelandProcess.add(newProcess);
            default -> interactivePriorityKernelandProcess.add(newProcess);
        }
    }
    /**
     * If something is running, process stops; if process didn't finish, adds to list of processes
     */
    public synchronized void SwitchProcess() {
        // as long as there are sleeping items, check if the process' wake time is up and give chance to run
        synchronized (sleepingProcessList) {
            while (!sleepingProcessList.isEmpty() && sleepingProcessList.peek().getValue().isBefore(Instant.now())) {
                KernelandProcess awake = sleepingProcessList.remove().getKey();
                if (awake != null) awake.run();
            }
        }
        if (currentKernelandProcess != null) { // process is running
            // put process onto correct queue
            if (!currentKernelandProcess.isDone()) addToRespectivePriorityQueue(currentKernelandProcess.getPriority(), currentKernelandProcess);
            currentKernelandProcess.stop();
        }
        // choose a random queue to pick from and run next process from there
        RunRespectivePriorityQueueAndDemote(ChooseRandomPriority());
    }

    /**
     * Chooses a random priority value from the Priority enums
     * @return A random priority enum value
     */
    private Priority ChooseRandomPriority() {
        return Priority.values()[new Random().nextInt(Priority.values().length)];
    }

    /**
     * Chooses the first item in the respective priority queue to run and then demotes it down one priority
     * @param priority A respective priority enum value
     */
    private synchronized void RunRespectivePriorityQueueAndDemote(Priority priority) {
        switch (priority) {
            case HIGH:
                if (!highPriorityKernelandProcess.isEmpty()) {
                    KernelandProcess firstItemInList = highPriorityKernelandProcess.remove();
                    DemoteProcess(firstItemInList);
                    firstItemInList.run();
                }
                break;
            case MEDIUM:
                if (!mediumPriorityKernelandProcess.isEmpty()) {
                    KernelandProcess firstItemInList = mediumPriorityKernelandProcess.remove();
                    DemoteProcess(firstItemInList);
                    firstItemInList.run();
                }
                break;
            case LOW:
                if (!lowPriorityKernelandProcess.isEmpty()) {
                    KernelandProcess firstItemInList = lowPriorityKernelandProcess.remove();
                    firstItemInList.run();
                    // keep at lowest level, no demote
                }
                break;
            default:
                if (!interactivePriorityKernelandProcess.isEmpty()) {
                    KernelandProcess firstItemInList = interactivePriorityKernelandProcess.remove();
                    firstItemInList.run();
                }
                break;
        }
    }

    /**
     * Demotes a process to the next lowest level; does nothing if the priority is already LOW or INTERACTIVE
     * @param kernelandProcess A certain KernelandProcess to demote
     */
    public synchronized void DemoteProcess(KernelandProcess kernelandProcess) {
        Priority p = kernelandProcess.getPriority();
        switch (p) {
            case HIGH -> mediumPriorityKernelandProcess.add(kernelandProcess);
            case MEDIUM -> lowPriorityKernelandProcess.add(kernelandProcess);
            default -> { }
        }
    }
    /**
     * Used to schedule an interrupt in Scheduler constructor
     */
    private class Interrupt extends TimerTask {
        @Override
        public void run() {
            SwitchProcess();
        }
    }

    /**
     * Adds the process to the list of sleeping processes based on time asleep, stops current process and switches processes
     * @param milliseconds Number of milliseconds to sleep
     */
    public void Sleep(int milliseconds) {
        synchronized (sleepingProcessList) { // add to list of sleeping processes
            sleepingProcessList.add(new AbstractMap.SimpleEntry<>(currentKernelandProcess, clock.plusMillis(milliseconds)));
        }
        if (currentKernelandProcess != null) { // stop process from running in meantime
            var tmp = currentKernelandProcess;
            currentKernelandProcess = null;
            tmp.stop();
        }
        SwitchProcess(); // let process go to sleep
    }
}
