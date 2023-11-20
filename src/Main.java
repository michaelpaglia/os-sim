public class Main {
    public static void main(String[] args) throws Exception {
        OS.Startup(new TestMemory());
        OS.CreateProcess(new TestMemory());
        OS.CreateProcess(new TestMemory());
        OS.CreateProcess(new TestMemory());
        OS.CreateProcess(new TestMemory());

        //OS.CreateProcess(new GoodbyeWorld());
    }
}