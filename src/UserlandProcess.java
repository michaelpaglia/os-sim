public abstract class UserlandProcess implements Runnable {
    static int[][] TLB = new int[2][2];
    static byte[] memory = new byte[1048576]; // 1024*1024 bytes
    abstract byte Read(int address);
    abstract void Write(int address, byte value);
}
