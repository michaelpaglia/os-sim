public class VirtualFileSystem implements Device {
    private final Device[] device;
    private final int[] id;
    public VirtualFileSystem() {
        this.device = new Device[10];
        this.id = new int[10];
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
        String[] arr = s.split(" ", 2);
        if (arr[0].equals("random")) {
            RandomDevice rd = new RandomDevice();
            return rd.Open(arr[1]);
        } else if (arr[0].equals("file")) {
            try {
                FakeFileSystem ffs = new FakeFileSystem(arr[1]);
                return ffs.Open(arr[1]);
            } catch (Exception e) { throw new RuntimeException(e); }
        } else return -1;
    }

    /**
     * Closes the device and ID entries
     * @param id Index to set to null or 0
     */
    @Override
    public void Close(int id) {
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
        return this.device[this.id[id]].Read(this.id[id], size);
    }

    /**
     * Pass id and to to appropriate device
     * @param id Device ID to seek from
     * @param to New offset from the beginning of the file
     */
    @Override
    public void Seek(int id, int to) {
        this.device[this.id[id]].Seek(this.id[id], to);
    }

    /**
     * Pass id and data to appropriate device
     * @param id Device ID to write to
     * @param data Byte array to write to
     * @return Number of bytes written
     */
    @Override
    public int Write(int id, byte[] data) {
        return this.device[this.id[id]].Write(this.id[id], data);
    }
}
