import java.time.Instant;
import java.util.*;

public class Scheduler {
    private final List<KernelandProcess> realTimeKernelandProcess, backgroundKernelandProcess, interactivePriorityKernelandProcess;
    private final List<Map.Entry<KernelandProcess, Instant>> sleepingProcess;
    private KernelandProcess currentKernelandProcess; // reference to KernelandProcess currently running
    private final Map<String, Integer> nameIdMapping;
    private final Map<Integer, KernelandProcess> processIdMapping;
    private final Map<Integer, KernelandProcess> waitingProcess;
    private final Instant clock;
    private static final Kernel kernel;
    static {
        try {
            kernel = new Kernel();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructs a scheduler which holds a list of processes, a timer instance, and a scheduled interrupt every 250ms
     */
    public Scheduler() {
        realTimeKernelandProcess = Collections.synchronizedList(new LinkedList<>());
        backgroundKernelandProcess = Collections.synchronizedList(new LinkedList<>());
        interactivePriorityKernelandProcess = Collections.synchronizedList(new LinkedList<>());
        sleepingProcess = Collections.synchronizedList(new LinkedList<>());
        nameIdMapping = Collections.synchronizedMap(new HashMap<>());
        processIdMapping = Collections.synchronizedMap(new HashMap<>());
        waitingProcess = Collections.synchronizedMap(new HashMap<>());
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
        this.nameIdMapping.put(newProcess.GetName(), newProcess.GetPid());
        this.processIdMapping.put(newProcess.GetPid(), newProcess);
        if (this.currentKernelandProcess == null) SwitchProcess();
        return newProcess.GetPid();
    }
    public synchronized KernelandProcess GetCurrentProcess() {
        return this.currentKernelandProcess;
    }
    /**
     * Adds a process to a specific priority queue
     * @param p A respective priority enum value
     * @param kp A respective KernelandProcess
     */
    private void AppendKernelandProcess(Priority p, KernelandProcess kp) {
        switch (p) {
            case REALTIME ->  realTimeKernelandProcess.add(kp);
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
            AppendKernelandProcess(awake.GetPriority(), awake);
        }
        if (this.currentKernelandProcess != null) {
            this.currentKernelandProcess.SetTimeout(this.currentKernelandProcess.GetTimeout() + 1);
            this.currentKernelandProcess.ClearTLB(); // Clear TLB on task switch
            this.currentKernelandProcess.Stop();

            if (!this.currentKernelandProcess.IsDone()) {
                if (this.currentKernelandProcess.GetTimeout() == 5) DemoteProcess(this.currentKernelandProcess);
                else AppendKernelandProcess(this.currentKernelandProcess.GetPriority(), this.currentKernelandProcess);
            } else { // process is done so close its open devices
                // Close all of its open devices
                this.currentKernelandProcess.ClearMemory(); // Terminate process clears all available memory
                this.nameIdMapping.remove(this.currentKernelandProcess.GetName());
                this.processIdMapping.remove(this.currentKernelandProcess.GetPid());
                int[] entries = this.currentKernelandProcess.GetKernelEntries();
                System.out.println("Process ended, closing open entries");
                for (int i=0; i<10; i++) {
                    if (entries[i] != -1) kernel.Close(i);
                }
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
                //System.out.println("Demoting from REALTIME to Interactive");
                kp.SetPriority(Priority.INTERACTIVE);
                interactivePriorityKernelandProcess.add(kp);
            }
            case INTERACTIVE -> {
                //System.out.println("Demoting from INTERACTIVE to BACKGROUND");
                kp.SetPriority(Priority.BACKGROUND);
                backgroundKernelandProcess.add(kp);
            }
            default -> backgroundKernelandProcess.add(kp);
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
                KernelandProcess firstItem = interactivePriorityKernelandProcess.remove(0);
                this.currentKernelandProcess = firstItem;
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
    public void AppendWaitingProcesses() {
        this.currentKernelandProcess.SetTimeout(0);
        waitingProcess.put(this.currentKernelandProcess.GetPid(), this.currentKernelandProcess);
        SwitchProcess();
    }
    public boolean IsWaiting(KernelandProcess kp) {
        return waitingProcess.containsValue(kp);
    }
    public void RestoreWaitingProcess(KernelandProcess kp) {
        KernelandProcess waiting = waitingProcess.remove(kp.GetPid());
        AppendKernelandProcess(waiting.GetPriority(), waiting);
        SwitchProcess();
    }
    /**
     * Returns the PID of the process
     * @return PID of the process
     */
    public int GetPid() {
        return this.currentKernelandProcess.GetPid();
    }
    /**
     * Returns the PID of the process with that name
     * @param s Name of the process
     * @return PID of the process with that name
     */
    public int GetPidByName(String s) {
        return nameIdMapping.get(s);
    }
    /**
     * Returns the KernelandProcess corresponding to a pid
     * @param pid The pid of a process
     * @return KernelandProcess corresponding to a PID
     */
    public KernelandProcess GetProcessByPid(int pid) {
        return this.processIdMapping.get(pid);
    }
    public KernelandProcess GetRandomProcess() {
        List<List<KernelandProcess>> nonEmptyLists = new ArrayList<>();
        if (!realTimeKernelandProcess.isEmpty()) nonEmptyLists.add(realTimeKernelandProcess);
        if (!backgroundKernelandProcess.isEmpty()) nonEmptyLists.add(backgroundKernelandProcess);
        if (!interactivePriorityKernelandProcess.isEmpty()) nonEmptyLists.add(interactivePriorityKernelandProcess);
        List<KernelandProcess> chooseRandomNonEmptyList = nonEmptyLists.get(new Random().nextInt(nonEmptyLists.size()));
        KernelandProcess randomProcess;
        int pageNumberToSwap;
        // Get random process and find a page that has physical memory.
        // If there are none, pick a different process and repeat until a new page is found
        do {
            randomProcess = chooseRandomNonEmptyList.get(new Random().nextInt(chooseRandomNonEmptyList.size()));
            System.out.println(randomProcess.virtualPageToPhysicalPage);
            pageNumberToSwap = FindPhysicalMemory(randomProcess); // Will be -1 if can't find page
        }while(pageNumberToSwap==-1);
        int oldRandomProcessPhysicalPage = currentKernelandProcess.virtualPageToPhysicalPage[pageNumberToSwap].physicalPageNumber;
        System.out.println("Swapping values of pages, old page="+oldRandomProcessPhysicalPage+" and new page index="+pageNumberToSwap);
        currentKernelandProcess.virtualPageToPhysicalPage[pageNumberToSwap].physicalPageNumber = randomProcess.virtualPageToPhysicalPage[pageNumberToSwap].physicalPageNumber;
        // Write victim page to disk and assign new block of swap file if there isn't already one
        kernel.GetVFS().GetFFS().Write(0, new byte[randomProcess.virtualPageToPhysicalPage[pageNumberToSwap].physicalPageNumber*1024]);
        // Swap
        randomProcess.virtualPageToPhysicalPage[pageNumberToSwap].physicalPageNumber = randomProcess.virtualPageToPhysicalPage[pageNumberToSwap].diskPageNumber;
        // Set victim physical page to -1
        randomProcess.virtualPageToPhysicalPage[pageNumberToSwap].physicalPageNumber = -1;
        if (oldRandomProcessPhysicalPage != currentKernelandProcess.virtualPageToPhysicalPage[pageNumberToSwap].physicalPageNumber) {
            // New physical page number
            if (currentKernelandProcess.virtualPageToPhysicalPage[pageNumberToSwap].diskPageNumber != -1) {
                // Load old data in and populate physical page
                System.out.println("Loading old data in and populating physical page");
                kernel.GetVFS().GetFFS().Write(0, kernel.GetVFS().Read(0, oldRandomProcessPhysicalPage*1024));
            } else {
                // Populate memory with zeros
                System.out.println("Populating with zeros");
                kernel.GetVFS().GetFFS().Write(0, new byte[0]);
            }
        }
        nonEmptyLists.clear();
        return randomProcess;
    }
    private int FindPhysicalMemory(KernelandProcess randomProcess) {
        for (int i = 0; i < randomProcess.virtualPageToPhysicalPage.length; i++) {
            System.out.println(randomProcess.virtualPageToPhysicalPage[i].physicalPageNumber);
            if (randomProcess.virtualPageToPhysicalPage[i].physicalPageNumber != -1) {
                return i;
            }
        }
        return -1;
    }
}
