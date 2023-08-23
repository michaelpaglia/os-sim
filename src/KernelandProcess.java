@SuppressWarnings("ALL")
public class KernelandProcess {
    private static int nextPid = 0;
    private int processId;
    boolean isThreadStarted;
    Thread pThread;

    KernelandProcess(UserlandProcess up) {
        this.pThread = new Thread(up);
        this.processId = nextPid++;
        this.isThreadStarted = false;
    }
    int getPid() {
        return this.processId;
    }
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
    void stop() {
        if (isThreadStarted) pThread.suspend();
    }
    boolean isDone() {
        return isThreadStarted && !pThread.isAlive();
    }
}
