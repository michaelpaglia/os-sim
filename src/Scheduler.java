import java.time.Instant;
import java.util.*;

public class Scheduler {
    private List<KernelandProcess> highPriorityKernelandProcess, mediumPriorityKernelandProcess, lowPriorityKernelandProcess, interactivePriorityKernelandProcess;
    private PriorityQueue<Map.Entry<KernelandProcess, Instant>> sleepingProcessList;
    private KernelandProcess currentKernelandProcess; // reference to KernelandProcess currently running
    private Instant clock;

    /**
     * Constructs a scheduler which holds a list of processes, a timer instance, and a scheduled interrupt every 250ms
     */
    public Scheduler() {
        highPriorityKernelandProcess = Collections.synchronizedList(new LinkedList<KernelandProcess>());
        mediumPriorityKernelandProcess = Collections.synchronizedList(new LinkedList<KernelandProcess>());
        lowPriorityKernelandProcess = Collections.synchronizedList(new LinkedList<KernelandProcess>());
        interactivePriorityKernelandProcess = Collections.synchronizedList(new LinkedList<KernelandProcess>());
        sleepingProcessList = new PriorityQueue<>(Map.Entry.comparingByValue());
        Timer timer = new Timer();
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
        AddKernelandProcessToList(priority, newProcess);
        if (currentKernelandProcess == null) SwitchProcess();
        return newProcess.getPid();
    }

    /**
     * Adds a process to a specific priority queue
     * @param priority A respective priority enum value
     * @param newProcess A respective KernelandProcess
     */
    private synchronized void AddKernelandProcessToList(Priority priority, KernelandProcess newProcess) {
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
                KernelandProcess awake = sleepingProcessList.remove().getKey(); // remove from sleeping processes and choose KernelandProcess
                if (awake != null) awake.run();
            }
        }
        if (currentKernelandProcess != null) { // process is running
            // put process onto correct queue
            if (!currentKernelandProcess.isDone()) AddKernelandProcessToList(currentKernelandProcess.getPriority(), currentKernelandProcess);
            currentKernelandProcess.stop();
        }
        // choose a random queue to pick from and run next process from there
        DemoteAndRunKernelandProcess(ChooseRandomPriority());
    }

    /**
     * Chooses a random, valid priority value from the Priority enums
     * @return A random priority enum value; selection is based on already-populated Priority processes
     */
    private Priority ChooseRandomPriority() {
        List<Priority> p = new LinkedList<>();
        if (!highPriorityKernelandProcess.isEmpty()) p.add(Priority.HIGH);
        if (!mediumPriorityKernelandProcess.isEmpty()) p.add(Priority.MEDIUM);
        if (!lowPriorityKernelandProcess.isEmpty()) p.add(Priority.LOW);
        if (!interactivePriorityKernelandProcess.isEmpty()) p.add(Priority.INTERACTIVE);
        //return Priority.values()[new Random().nextInt(Priority.values().length)];
        return p.get(new Random().nextInt(p.size()));
    }

    /**
     * Chooses the first item in the respective priority queue to run and then demotes it down one priority
     * @param priority A respective priority enum value
     */
    private synchronized void DemoteAndRunKernelandProcess(Priority priority) {
        switch (priority) {
            case HIGH:
                if (!highPriorityKernelandProcess.isEmpty()) {
                    KernelandProcess firstItemInList = highPriorityKernelandProcess.remove(0);
                    mediumPriorityKernelandProcess.add(firstItemInList);
                    firstItemInList.run();
                }
                break;
            case MEDIUM:
                if (!mediumPriorityKernelandProcess.isEmpty()) {
                    KernelandProcess firstItemInList = mediumPriorityKernelandProcess.remove(0);
                    lowPriorityKernelandProcess.add(firstItemInList);
                    firstItemInList.run();
                }
                break;
            case LOW:
                if (!lowPriorityKernelandProcess.isEmpty()) {
                    KernelandProcess firstItemInList = lowPriorityKernelandProcess.remove(0);
                    firstItemInList.run();
                    // keep at lowest level, no demote
                }
                break;
            default:
                if (!interactivePriorityKernelandProcess.isEmpty()) {
                    KernelandProcess firstItemInList = interactivePriorityKernelandProcess.remove(0);
                    firstItemInList.run();
                }
                break;
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
    public synchronized void Sleep(int milliseconds) {
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
