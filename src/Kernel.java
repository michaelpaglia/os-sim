public class Kernel implements Device {
    private static Scheduler pScheduler;
    private static VirtualFileSystem VFS;
    private boolean[] inUseMemoryBlock;
    private static int nextPage;
    /**
     * Constructs a new scheduler
     */
    public Kernel() throws Exception {
        pScheduler = new Scheduler();
        VFS = new VirtualFileSystem();
        inUseMemoryBlock = new boolean[100]; // All values are initially False
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

    /**
     * Maps a virtual page number to a physical page number in TLB
     * @param virtualPageNumber Some virtual page number to map
     */
    public void GetMapping(int virtualPageNumber) {
        KernelandProcess kp = pScheduler.GetCurrentProcess();
        System.out.println("Setting the mapping of Kernel virtual page " + virtualPageNumber);
        kp.SetRandomTLB(virtualPageNumber);
    }

    /**
     * Allocates memory to inUseMemoryBlock by calculating pages needed, the start page in contiguous memory; maps physical page number
     * @param size Amount of memory (multiple of 1024) to allocate
     * @return Start virtual address in contiguous memory
     */
    public int AllocateMemory(int size) {
        int pagesNeeded = size/1024;
        int startPage = FindAvailableMemory(pagesNeeded);
        KernelandProcess kp = pScheduler.GetCurrentProcess();

        if (startPage == -1) return -1; // Failed to allocate memory
        else if (startPage + pagesNeeded <= inUseMemoryBlock.length) {
            System.out.println("Found contiguous block of memory at starting page " + startPage + ", we need " + pagesNeeded + " pages");
            for (int i=startPage; i < startPage + pagesNeeded; i++) {
                inUseMemoryBlock[i] = true;
                kp.SetProcessPhysicalPageNumber(i, kp.GetPhysicalPageNumber(i)); // set correct page number in process' array
            }
        }
        return startPage*1024; // Starting virtual address
    }

    /**
     * Frees allocated memory, removes mapping from process array
     * @param pointer Starting virtual address to free
     * @param size Amount of memory to free
     * @return True if memory was freed successfully
     */
    public boolean FreeMemory(int pointer, int size) {
        int virtualPageIndex = pointer/1024; // Gives page number in array
        int pagesToFree = size/1024; // Number of pages to free
        KernelandProcess kp = pScheduler.GetCurrentProcess();

        // Set all memory in range [virtualPageIndex, virtualPageIndex+pagesToFree] to false
        for (int i=virtualPageIndex; i < virtualPageIndex+pagesToFree; i++) {
            System.out.println("Attempting to free virtual page index " + virtualPageIndex + " with # pages to free " + pagesToFree);
            inUseMemoryBlock[i] = false;
            kp.SetProcessPhysicalPageNumber(i, -1); // Remove from process array, set back to -1
        }
        return true; // Memory successfully freed
    }

    /**
     * Searches through the inUseMemoryBlock to find /pages/ of free space
     * @param pages Amount of pages needed in memory
     * @return Index in memory block to start; -1 on failure (all memory is used/not enough window space)
     */
    private int FindAvailableMemory(int pages) {
        int idx = -1;
        int window = 0;
        for (int i=0; i < inUseMemoryBlock.length; i++) {
            // True = memory is not available
            // False = memory is available
            if (!inUseMemoryBlock[i]) { // False, memory is available
                if (idx == -1) idx = i;
                window++;
            } else { // Memory is not available, reset
                idx = -1;
                window = 0;
            }
            if (window >= pages) return idx; // Window size is big enough to fit the required pages
        }
        return -1;
    }
}
