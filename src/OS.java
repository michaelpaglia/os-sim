enum Priority {
    REALTIME, BACKGROUND, INTERACTIVE
}
public class OS {
    private static Kernel pKernel; // reference to one and only instance of Kernel

    /**
     * Populates the Kernel member with a new instance and calls CreateProcess on init
     * @param init Initialize a new UserlandProcess
     */
    public static void Startup(UserlandProcess init) {
        pKernel = new Kernel();
        CreateProcess(init);
    }

    /**
     * Calls Kernel's CreateProcess()
     * @param up Create a new UserlandProcess
     * @return PID of created process
     */
    public static int CreateProcess(UserlandProcess up) {
        return pKernel.CreateProcess(up);
    }

    public static int CreateProcess(UserlandProcess up, Priority priority) {
        return pKernel.CreateProcess(up, priority);
    }
    public static void Sleep(int milliseconds) {
        pKernel.Sleep(milliseconds);
    }
}
