import java.util.*;

public class RandomDevice implements Device {
    private final Random[] randomDevice;
    public RandomDevice() {
        this.randomDevice = new Random[10];
        Arrays.fill(randomDevice, null);
    }
    /**
     * Creates a new Random device and put it in an empty spot in the array.
     * If the supplied string for Open is not null or empty, assume that it is the seed for the Random class (convert the string to an integer).
     * @param s Some generic device
     * @return Index in the array of new Random
     */
    @Override
    public int Open(String s) {
        System.out.println("Opening random device, finding location");
        for (int i=0; i<10; i++) {
            if (randomDevice[i] == null) {
                if (s != null) randomDevice[i] = new Random(Integer.parseInt(s));
                else randomDevice[i] = new Random();
                System.out.print("Opened random device at random device index " + i + "\n");
                return i;
            }
        }
        System.out.println("Can't open device, all entries are filled");
        return -1; // unsuccessful open, all entries are filled
    }

    /**
     * Null the device entry
     * @param id Index in the array (entry to null)
     */
    @Override
    public void Close(int id) {
        System.out.println("Closing random device at index " + id);
        randomDevice[id] = null;
    }

    /**
     * Creates/fills an array with random values
     * @param id Index of device in RandomDevice array
     * @param size Size of the newly created/filled array
     * @return Newly created byte array
     */
    @Override
    public byte[] Read(int id, int size) {
        synchronized (this.randomDevice) {
            System.out.println("Reading from random device at id " + id);
            byte[] create = new byte[size];
            randomDevice[id].nextBytes(create);
            System.out.println("Created random device byte array of size " + size + " " + create);
            return create;
        }
    }

    /**
     * Reads random bytes
     * @param id Index of device in RandomDevice array
     * @param to New position within stream
     */
    @Override
    public void Seek(int id, int to) {
        // i don't think this is right
        Read(id, to);
    }

    @Override
    public int Write(int id, byte[] data) {
        return 0;
    }
}
