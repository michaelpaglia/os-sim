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
        Arrays.fill(kernelEntries, -1); // Fill array with empty entries
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
        Arrays.fill(kernelEntries, -1); // Fill array with empty entries
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
}
