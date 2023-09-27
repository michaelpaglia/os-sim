import java.util.Arrays;

@SuppressWarnings("ALL")
public class KernelandProcess {
    private static int nextPid = 0;
    private int processId, timeout;
    boolean isThreadStarted; // indicates whether thread has been started or not
    Thread pThread;
    private Priority priority;
    private final int[] kernelEntries;
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
        Arrays.fill(kernelEntries, -1); // Fill array with empty entries
    }
    KernelandProcess(UserlandProcess up, Priority priority) {
        this.pThread = new Thread(up);
        this.priority = priority;
        this.processId = nextPid++;
        this.isThreadStarted = false;
        this.timeout = 0;
        this.kernelEntries = new int[10];
        Arrays.fill(kernelEntries, -1); // Fill array with empty entries
    }
    int[] GetKernelEntries() { return this.kernelEntries; }
    void SetKernelEntries(int index, int val) { this.kernelEntries[index] = val; }
    int GetPID() { return this.processId;}
    Priority GetPriority() { return this.priority; }

    void SetPriority(Priority priority) { this.priority = priority; }

    int GetTimeout() { return this.timeout; }

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
}
