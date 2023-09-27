public class Main {
    public static void main(String[] args) {
        OS.Startup(new HelloWorld());
        OS.CreateProcess(new GoodbyeWorld(), Priority.REALTIME);
        OS.CreateProcess(new HelloWorld(), Priority.BACKGROUND);
        OS.CreateProcess(new GoodbyeWorld(), Priority.INTERACTIVE);
        OS.CreateProcess(new HelloWorld(), Priority.REALTIME);
    }
}