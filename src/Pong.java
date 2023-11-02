public class Pong extends UserlandProcess {
    @Override
    public void run() {
        int pongPid = OS.GetPid();
        int pingPid = OS.GetPidByName("Ping");
        KernelMessage pong = new KernelMessage(pongPid, pingPid, 0, new byte[5]);

        while (true) {
            KernelMessage wait = OS.WaitForMessage();
            if (wait != null) {
                System.out.println("Ping: " + wait);
                System.out.println("Pong: " + pong);
                OS.SendMessage(pong);
            }
            OS.Sleep(50);
        }
    }

    @Override
    byte Read(int address) {
        return 0;
    }

    @Override
    void Write(int address, byte value) {

    }
}
