enum Priority {
    REALTIME, BACKGROUND, INTERACTIVE
}
public class OS {
    static Kernel pKernel; // reference to one and only instance of Kernel

    /**
     * Populates the Kernel member with a new instance and calls CreateProcess on init
     * @param init Initialize a new UserlandProcess
     */
    public static void Startup(UserlandProcess init) throws Exception {
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
    public static int Open(String s) { return pKernel.Open(s); }
    public static void Close(int id) { pKernel.Close(id); }
    public static byte[] Read(int id, int size) { return pKernel.Read(id, size); }
    public static void Seek(int id, int to) { pKernel.Seek(id, to); }
    public static void Write(int id, byte[] data) { pKernel.Write(id, data); }

    /**
     * Returns the current process PID
     * @return PID of the current process
     */
    public static int GetPid() {
        return pKernel.GetPid();
    }

    /**
     * Returns the PID of the process with that name
     * @param name Name of the process
     * @return PID of the process with that name
     */
    public static int GetPidByName(String name) {
        return pKernel.GetPidByName(name);
    }
    public static void SendMessage(KernelMessage km) {
        pKernel.SendMessage(km);
    }
    public static KernelMessage WaitForMessage() {
        return pKernel.WaitForMessage();
    }

    public static void GetMapping(int virtualPageNumber) { return pKernel.GetMapping(virtualPageNumber); }
}
