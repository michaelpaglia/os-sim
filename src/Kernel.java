public class Kernel implements Device {
    private static Scheduler pScheduler;
    private static VirtualFileSystem VFS;
    /**
     * Constructs a new scheduler
     */
    public Kernel() throws Exception {
        pScheduler = new Scheduler();
        VFS = new VirtualFileSystem();
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
        KernelandProcess kp = pScheduler.GetCurrentProcess();
        int[] entries = kp.GetKernelEntries();
        for (int i=0; i<10; i++) {
            if (entries[i] == -1) {
                int openId = VFS.Open(s);
                if (openId == -1) return -1;
                // Put ID from VFS into KernelandProcess' array
                kp.SetKernelEntries(i, openId);
                return openId;
            }
        }
        return -1; // No entry available
    }

    /**
     * Uses KernelandProcess array to convert to valid ID for VFS.Close(id)
     * Sets id in KernelandProcess array to -1 (empty slot)
     * @param id Id of the entry to close
     */
    @Override
    public void Close(int id) {
        KernelandProcess kp = pScheduler.GetCurrentProcess();
        int[] entries = kp.GetKernelEntries();
        if (entries[id] != -1) {
            VFS.Close(id);
            kp.SetKernelEntries(id, -1);
        }
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
        if (entries[id] == -1) return new byte[0];
        return VFS.Read(id, size);
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
        System.out.println("Kernel write to id " + entries[id]);
        if (entries[id] == -1) return 0;
        return VFS.Write(id, data);
    }
    /**
     * Returns the PID of the process
     * @return PID of the process
     */
    public int GetPid() {
        return pScheduler.GetPid();
    }
    /**
     * Returns the PID of the process with that name
     * @param s Name of the process
     * @return PID of the process with that name
     */
    public int GetPidByName(String s) {
        return pScheduler.GetPidByName(s);
    }

    /**
     * Uses the constructor to make a copy of the original message and populates the sender's pid
     * Finds the target's KernelandProcess
     * If target's KernelandProcess pid is found, add to message queue
     * If KernelandProcess is waiting for a message, restore to its proper runnable queue
     * @param km KernelMessage to make a copy of
     */
    public void SendMessage(KernelMessage km) {
        KernelMessage copy = new KernelMessage(km);
        KernelandProcess target = pScheduler.GetProcessByPid(copy.targetPid);
        target.kernelMessage.add(copy);
        if (pScheduler.IsWaiting(target)) pScheduler.RestoreWaitingProcess(target);
    }

    /**
     * Checks to see if current process has a message
     * If so, returns the message off the queue; else, add process to new data structure to hold "waiting" processes
     * @return KernelMessage if the current process has a message
     */
    public KernelMessage WaitForMessage() {
        if (!pScheduler.GetCurrentProcess().kernelMessage.isEmpty()) {
            return pScheduler.GetCurrentProcess().kernelMessage.remove(0);
        }
        if (pScheduler.GetCurrentProcess() != null) pScheduler.AppendWaitingProcesses();

        return null;
    }

    public void GetMapping(int virtualPageNumber) {

        return;
    }
}
