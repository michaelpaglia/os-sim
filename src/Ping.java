public class Ping extends UserlandProcess {
    @Override
    public void run() {
        int pingPid = OS.GetPid();
        int pongPid = OS.GetPidByName("Pong");
        KernelMessage ping = new KernelMessage(pingPid, pongPid, 0, new byte[5]);

        while (true) {
            OS.SendMessage(ping);
            OS.Sleep(50);
        }
    }
}
