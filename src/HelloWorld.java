public class HelloWorld extends UserlandProcess {
    @Override
    public void run() {
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
    byte Read(int address) {
        // The first thing that either of these methods need to do is find the page number.
        // That is easy – address/page size. Next, they need to look at the TLB to see if this virtual page : physical page mapping is in there.
        // The TLB (which, of course, normally is in hardware) should be a static array of integer holding 2 virtual addresses and 2 physical addresses.
        // I would use a [2][2] array, but there are other schemes which work.
        // If the mapping is found, then we need to calculate the physical address.
        // Remember that the virtual page is virtual address/1024.
        // Where we are within the page is virtual address % 1024.
        // Once we know the physical page number, we multiply it by the page size and add the page offset to get the physical address.
        int pageNumber = address/1024; // Page size is 1024 or 1KB
        int virtualOffset = address%1024;
        for (int i=0; i<TLB.length; i++) {
            if (TLB[i][0] == pageNumber) { // Page number is found, check mapping
                int physicalPageNumber = TLB[i][1];
                int physicalAddress = physicalPageNumber*1024+virtualOffset; // Retrieve physical address based on mapping
                return memory[physicalAddress];
            }
        }
        // not found, perform OS call
    }

    @Override
    void Write(int address, byte value) {

    }
}
