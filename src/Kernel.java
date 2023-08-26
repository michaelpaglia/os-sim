public class Kernel {
    static Scheduler pScheduler;

    /**
     * Constructs a new scheduler
     */
    public Kernel() {
        pScheduler = new Scheduler();
    }

    /**
     * Calls Scheduler's CreateProcess()
     * @param up Create a new UserlandProcess
     * @return PID of created process
     */
    public static int CreateProcess(UserlandProcess up) {
        return pScheduler.CreateProcess(up);
    }
    public static int CreateProcess(UserlandProcess up, Priority priority) {
        return pScheduler.CreateProcess(up, priority);
    }
    public static void Sleep(int milliseconds) {
        pScheduler.Sleep(milliseconds);
    }
}
