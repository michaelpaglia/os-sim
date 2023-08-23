public class Kernel {
    static Scheduler pScheduler;

    public Kernel() {
        pScheduler = new Scheduler();
    }
    public static int CreateProcess(UserlandProcess up) {
        return pScheduler.CreateProcess(up);
    }
}
