public class HelloWorld extends UserlandProcess {
    @Override
    public void run() {
        Write(3100, (byte) 5);
        //Read(3100); // Read from address already in TLB
        int startAddr = OS.AllocateMemory(3072);
        OS.FreeMemory(startAddr, 2048);
        while (true) { // infinite loop as per assignment requirements
            System.out.println("Hello World");
//            int openfd = OS.Open("file test.txt");
//            if (openfd != -1) {
//                OS.Write(openfd, new byte[10]);
//                OS.Read(openfd, 10);
//            } else OS.Close(0);
            OS.Sleep(50);
        }
    }

    @Override
    public byte Read(int address) {
        int pageNumber = address/1024; // Page size is 1024 or 1KB
        int virtualOffset = address%1024;
        for (int[] ints : TLB) {
            if (ints[0] == pageNumber) { // Page number is found, check mapping
                int physicalPageNumber = ints[1];
                int physicalAddress = physicalPageNumber * 1024 + virtualOffset; // Retrieve physical address based on mapping
                System.out.println("Mapping is found in TLB: byte value " + memory[physicalAddress]);
                return memory[physicalAddress];
            }
        }
        // not found, perform OS call and retry with new TLB
        OS.GetMapping(pageNumber);
        return Read(pageNumber);
    }

    @Override
    public void Write(int address, byte value) {
        int pageNumber = address/1024; // Page size is 1024 or 1KB
        int virtualOffset = address%1024;

        for (int[] ints : TLB) {
            if (ints[0] == pageNumber) { // Page number is found, check mapping
                int physicalPageNumber = ints[1];
                int physicalAddress = physicalPageNumber * 1024 + virtualOffset; // Retrieve physical address based on mapping
                System.out.println("TLB mapping is found... " + address);
                memory[physicalAddress] = value;
                return;
            }
        }
        // TLB mapping not found, retry
        System.out.println("TLB mapping is not found, writing to new random spot in memory... " + address);
        OS.GetMapping(pageNumber);
        Write(address, value);
    }
}
