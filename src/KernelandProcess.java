@SuppressWarnings("ALL")
public class KernelandProcess {
    private static int nextPid = 0;
    private int processId, timeout;
    boolean isThreadStarted; // indicates whether thread has been started or not
    Thread pThread;
    private Priority priority;
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
    }
    KernelandProcess(UserlandProcess up, Priority priority) {
        this.pThread = new Thread(up);
        this.priority = priority;
        this.processId = nextPid++;
        this.isThreadStarted = false;
        this.timeout = 0;
    }

    /**
     * Accesses the current process' PID
     * @return Given PID
     */
    int getPid() {
        return this.processId;
    }

    Priority getPriority() { return this.priority; }

    void setPriority(Priority priority) { this.priority = priority; }

    int getTimeout() { return this.timeout; }

    void setTimeout(int timeout) { this.timeout = timeout; }

    /**
     * Resumes thread if already started, else, sets flag to true and starts thread
     */
    void run() {
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
    void stop() {
        if (isThreadStarted) pThread.suspend();
    }

    /**
     * Flags whether thread is already started and is not alive (completed)
     * @return True if thread is already started and is not alive
     */
    boolean isDone() {
        return isThreadStarted && !pThread.isAlive();
    }
}
