import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class FakeFileSystem implements Device {
    private final RandomAccessFile[] fakeFile;
    public FakeFileSystem(String filename) throws Exception {
        if (filename == null || filename.isEmpty()) throw new Exception("File name is null or empty");
        this.fakeFile = new RandomAccessFile[10];
        Arrays.fill(fakeFile, null);
    }

    /**
     * Creates and records a random access file in the array
     * @param s Some device to open
     * @return Records position of RandomAccessFile in the array
     */
    @Override
    public int Open(String s) {
        System.out.println("Opening fake file, finding location");
        for (int i=0; i<10; i++) {
            if (fakeFile[i] == null) {
                // already check filename to see if it is null or empty
                try {
                    // after second iteration, file location is still 0
                    fakeFile[i] = new RandomAccessFile(s, "rw");
                    System.out.println("Fake file location " + i);
                    return i;
                }
                catch (FileNotFoundException e) { throw new RuntimeException(e); }
            }
        }
        System.out.println("Fake File: no entries available");
        return -1;
    }

    /**
     * Closes the RandomAccessFile stream
     * @param id Some RandomAccessFile device
     */
    @Override
    public void Close(int id) {
        System.out.println("Closing fake file at id " + id);
        try {
            fakeFile[id].close();
            fakeFile[id] = null;
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Creates a byte array "b" of length "size" and reads up to "size" bytes of data
     * @param id Some RandomAccessFile
     * @param size Size of byte array to create
     * @return Filled byte array or null if reached end of the file
     */
    @Override
    public byte[] Read(int id, int size) {
        System.out.println("Reading from fake file at id " + id);
        byte[] create = new byte[size];
        try {
            if (fakeFile[id].read(create) > 0) return create; // not end of the file
            return null;
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Sets the offset from where the next read will occur
     * @param id Some RandomAccessFile
     * @param to New offset from the beginning of the file
     */
    @Override
    public void Seek(int id, int to) {
        try { fakeFile[id].seek(to); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    /**
     * Writes data.length bytes to a file
     * @param id Some RandomAccessFile
     * @param data Specified byte array to write from
     * @return Number of bytes written
     */
    @Override
    public int Write(int id, byte[] data) {
        System.out.println("Writing to a fake file " + id + " " + fakeFile[id]); // this is the problem... it will always be 0
        try {
            fakeFile[id].write(data);
            System.out.println("Wrote " + data.length + " bytes to fake file");
            return data.length;
        }
        catch (IOException e) { throw new RuntimeException(e); }
    }
}
