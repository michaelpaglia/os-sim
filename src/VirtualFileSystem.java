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
        String arr[] = s.split(" ", 2);
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

    @Override
    public void Close(int id) {

    }

    @Override
    public byte[] Read(int id, int size) {
        return new byte[0];
    }

    @Override
    public void Seek(int id, int to) {

    }

    @Override
    public int Write(int id, byte[] data) {
        return 0;
    }
}
