public class Main {
    public static void main(String[] args) {
        OS.Startup(new HelloWorld());
        OS.Sleep(5000);

        OS.CreateProcess(new GoodbyeWorld(), Priority.REALTIME);
        OS.Sleep(2500);
    }
}