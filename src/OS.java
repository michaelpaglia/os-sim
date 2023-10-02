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
}
