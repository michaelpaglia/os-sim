import java.util.Arrays;
public class VirtualFileSystem implements Device {
    private final Device[] device;
    private final int[] id;
    private static FakeFileSystem ffs;
    private static RandomDevice rd;
    public VirtualFileSystem() throws Exception {
        this.device = new Device[10];
        this.id = new int[10];
        Arrays.fill(this.device, null);
        Arrays.fill(this.id, -1);
        ffs = new FakeFileSystem("file");
        rd = new RandomDevice();
    }

    /**
     * First word of the input string is the device, the rest is passed to the device
     * Examines first word, removes that from string and passes the remainder to the
     * respective device.
     * @param s Some device input
     * @return Index of opened device entry, -1 if invalid input
     */
    @Override
    public int Open(String s) {
        synchronized (this.device) {
            String[] arr = s.split(" ", 2);
            if (arr[0].equals("random")) {
                System.out.println("Opening Random from VFS for file " + arr[1]);
                for (int i = 0; i < 10; i++) {
                    if (this.device[i] == null && this.id[i] == -1) {
                        System.out.println("------Calling random device from VFS at index " + i + "------\n");
                        this.device[i] = rd;
                        this.id[i] = i;
                        return this.device[i].Open(arr[1]);
                    }
                }
            } else if (arr[0].equals("file")) {
                System.out.println("Opening Fake File System from VFS for file " + arr[1]);
                for (int i = 0; i < 10; i++) {
                    if (this.device[i] == null && this.id[i] == -1) {
                        try {
                            System.out.println("------Calling fake file at index " + i + "------\n");
                            this.device[i] = ffs;
                            this.id[i] = i;
                            return this.device[i].Open(arr[1]);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        System.out.println("------------VFS: No entries available to open------------");
        return -1;
    }

    /**
     * Closes the device and ID entries
     * @param id Index to set to null or 0
     */
    @Override
    public void Close(int id) {
        System.out.println("VFS closing id " + id);
        int newId = this.id[id];
        if (this.device[id] != null) this.device[newId].Close(id);
        this.device[id] = null;
        this.id[id] = -1;
    }

    /**
     * Pass id and size to appropriate device
     * @param id Device ID to read from (RandomAccessFile or FakeFileSystem)
     * @param size Size of the newly created/filled array
     * @return Newly created byte array
     */
    @Override
    public byte[] Read(int id, int size) {
        System.out.println("VFS reading at id " + id + "\n");
        synchronized (this.device) {
            int newId = this.id[id];
            System.out.print("This device: " + this.device[id] + "\n");
            if (newId == -1) return new byte[0];
            return this.device[newId].Read(id, size);
        }
    }

    /**
     * Pass id and to to appropriate device
     * @param id Device ID to seek from
     * @param to New offset from the beginning of the file
     */
    @Override
    public void Seek(int id, int to) {
        int newId = this.id[id];
        this.device[newId].Seek(newId, to);
    }

    /**
     * Pass id and data to appropriate device
     * @param id Device ID to write to
     * @param data Byte array to write to
     * @return Number of bytes written
     */
    @Override
    public int Write(int id, byte[] data) {
        System.out.println("VFS writing at id " + id);
        synchronized (this.device) {
            int newId = this.id[id];
            Device dev = this.device[id];
            if (newId == -1) return 0;
            return dev.Write(newId, data);
        }
    }
    public FakeFileSystem GetFFS() {
        return ffs;
    }
    public RandomDevice GetRandomDevice() {
        return rd;
    }
}
