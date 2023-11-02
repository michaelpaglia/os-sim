import java.util.Arrays;
import java.util.LinkedList;

@SuppressWarnings("ALL")
public class KernelandProcess {
    private static int nextPid = 0;
    private int processId, timeout;
    boolean isThreadStarted; // indicates whether thread has been started or not
    Thread pThread;
    private Priority priority;
    private final int[] kernelEntries;
    String name;
    LinkedList<KernelMessage> kernelMessage;
    private int[] virtualPageToPhysicalPage; // Index is virtual page number, value is physical page number
    private UserlandProcess userlandProcess;
    /**
     * Constructs a KernelandProcess by instantiating a new thread,
     * incrementing the most recent process ID, and declaring it not yet started
     * @param up A certain UserlandProcess
     */
    KernelandProcess(UserlandProcess up) {
        this.pThread = new Thread(up);
        this.processId = nextPid++;
        this.isThreadStarted = false;
        this.timeout = 0;
        this.kernelEntries = new int[10];
        this.name = up.getClass().getSimpleName();
        this.kernelMessage = new LinkedList<>();
        this.virtualPageToPhysicalPage = new int[100]; // 100 elements represents 100 pages
        this.userlandProcess = up;
        Arrays.fill(kernelEntries, -1); // Fill array with empty entries
        Arrays.fill(virtualPageToPhysicalPage, -1);
    }
    KernelandProcess(UserlandProcess up, Priority priority) {
        this.pThread = new Thread(up);
        this.priority = priority;
        this.processId = nextPid++;
        this.isThreadStarted = false;
        this.timeout = 0;
        this.kernelEntries = new int[10];
        this.name = up.getClass().getSimpleName();
        this.kernelMessage = new LinkedList<>();
        this.virtualPageToPhysicalPage = new int[100]; // 100 elements represents 100 pages
        Arrays.fill(kernelEntries, -1); // Fill array with empty entries
        Arrays.fill(virtualPageToPhysicalPage, -1);
    }

    /**
     * Retrieves the kernel entries of the given kernelandprocess
     * @return Integer array of kernel entries
     */
    int[] GetKernelEntries() { return this.kernelEntries; }

    /**
     * Sets an index of the kernelandprocess' entries array to a certain value
     * @param index Index of the kernelandprocess entry array
     * @param val Value to set in array
     */
    void SetKernelEntries(int index, int val) { this.kernelEntries[index] = val; }

    /**
     * Retrieves the PID of the kernelandprocess
     * @return PID of kernelandprocess
     */
    int GetPid() { return this.processId;}

    /**
     * Retrieves the name of the kernelandprocess
     * @return Name of kerneland process
     */
    String GetName() {
        return this.name;
    }

    /**
     * Retrieves the priority of the kernelandprocess
     * @return Priority of the kernelandprocess
     */
    Priority GetPriority() { return this.priority; }

    /**
     * Sets the kernelandprocess priority
     * @param priority Priority to set kernelandprocess to
     */
    void SetPriority(Priority priority) { this.priority = priority; }

    /**
     * Retrieves the timeout value of the kernelandprocess
     * @return Timeout of the kernelandprocess
     */
    int GetTimeout() { return this.timeout; }
    /**
     * Sets the timeout of the kernelandprocess
     * @param timeout Timeout value to set to kernelandprocess
     */
    void SetTimeout(int timeout) { this.timeout = timeout; }

    /**
     * Resumes thread if already started, else, sets flag to true and starts thread
     */
    void Run() {
        if (isThreadStarted) {
            // already started, so resume
            pThread.resume();
        } else {
            // not yet started
            isThreadStarted = true;
            pThread.start();
        }
    }

    /**
     * Suspends thread if already started
     */
    void Stop() {
        if (isThreadStarted) pThread.suspend();
    }

    /**
     * Flags whether thread is already started and is not alive (completed)
     * @return True if thread is already started and is not alive
     */
    boolean IsDone() {
        return isThreadStarted && !pThread.isAlive();
    }

    /**
     * Randomly chooses an index in TLB to set a random virtual page number
     * @param virtualPageNumber Virtual page number to map
     */
    void SetRandomTLB(int virtualPageNumber) {
        int randomPhysicalPageNumber = (int) (Math.random() * 10); // Random page to update
        // Randomly update a TLB entry
        System.out.println("Updating random TLB entry in KernelandProcess to physical page number " + randomPhysicalPageNumber);
        int randomTLBIndex = (int) (Math.random() * 2);
        userlandProcess.TLB[randomTLBIndex][0] = virtualPageNumber;
        userlandProcess.TLB[randomTLBIndex][1] = randomPhysicalPageNumber;
    }

    /**
     * Map a virtual page number (index) to a physical page number (value) in Page Table
     * @param virtualPageNumber Some virtual page number to map
     * @param physicalPageNumber Some physical page number to map
     */
    void SetProcessPhysicalPageNumber(int virtualPageNumber, int physicalPageNumber) {
        virtualPageToPhysicalPage[virtualPageNumber] = physicalPageNumber;
    }

    /**
     * Retrieves some physical page number based on virtual page number mapping
     * @param virtualPageNumber Some virtual page number to find mapping
     * @return Physical page number if found in TLB; -1 on failure
     */
    int GetPhysicalPageNumber(int virtualPageNumber) {
        for (int i=0; i<userlandProcess.TLB.length; i++) {
            if (userlandProcess.TLB[i][0] == virtualPageNumber) {
                System.out.println("Found physical page number");
                return userlandProcess.TLB[i][1];
            }
        }
        return -1;
    }

    /**
     * Clears the TLB on a task switch
     */
    void ClearTLB() {
        System.out.println("Clearing TLB");
        Arrays.fill(userlandProcess.TLB[0], -1);
        Arrays.fill(userlandProcess.TLB[1], -1);
    }

    /**
     * Clears the memory on process termination
     */
    void ClearMemory() {
        System.out.println("Clearing memory");
        Arrays.fill(userlandProcess.memory, (byte) -1);
    }
}
