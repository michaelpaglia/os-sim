public class Kernel implements Device {
    static Scheduler pScheduler;
    private VirtualFileSystem VFS = new VirtualFileSystem();

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

    /**
     * Finds an empty entry in KernelandProcess' array, calls Open from VFS
     * Puts ID from VFS into KernelandProcess' array and returns the index
     * @param s Some device to open
     * @return Index of empty entry in KernelandProcess array
     */
    @Override
    public int Open(String s) {
        int[] entries = pScheduler.GetCurrentProcess().GetKernelEntries();
        for (int i=0; i<10; i++) {
            if (entries[i] == -1) {
                int openId = VFS.Open(s);
                if (openId == -1) return -1;
                // Put ID from VFS into KernelandProcess' array
                pScheduler.GetCurrentProcess().SetKernelEntries(i, openId);
                return i;
            }
        }
        return -1; // No empty entry available
    }

    /**
     * Uses KernelandProcess array to convert to valid ID for VFS.Close(id)
     * Sets id in KernelandProcess array to -1 (empty slot)
     * @param id
     */
    @Override
    public void Close(int id) {
        int[] entries = pScheduler.GetCurrentProcess().GetKernelEntries();
        VFS.Close(entries[id]);
        pScheduler.GetCurrentProcess().SetKernelEntries(entries[id], -1);
    }

    public void CloseAll() {
        int[] entries = pScheduler.GetCurrentProcess().GetKernelEntries();
        for (int i=0; i<10; i++) { if (entries[i] != -1) Close(i); }
    }

    /**
     * Uses KernelandProcess array to convert to valid ID for VFS.Read(id, size)
     * @param id Some entry id in the KernelandProcess array
     * @param size Size of the newly created/filled array
     * @return Newly created byte array
     */
    @Override
    public byte[] Read(int id, int size) {
        int[] entries = pScheduler.GetCurrentProcess().GetKernelEntries();
        return VFS.Read(entries[id], size);
    }

    /**
     * Uses KernelandProcess array to convert to valid ID for VFS.Seek(id, to)
     * @param id Some entry id in KernelandProcess array
     * @param to New position within stream
     */
    @Override
    public void Seek(int id, int to) {
        int[] entries = pScheduler.GetCurrentProcess().GetKernelEntries();
        VFS.Seek(entries[id], to);
    }
    /**
     * Uses KernelandProcess array to convert to valid ID for VFS.Write(id, data)
     * @param id Some entry id in KernelandProcess array
     * @param data Some byte array
     * @return Number of bytes written
     */
    @Override
    public int Write(int id, byte[] data) {
        int[] entries = pScheduler.GetCurrentProcess().GetKernelEntries();
        return VFS.Write(entries[id], data);
    }
}
