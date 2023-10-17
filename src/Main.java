public class Main {
    public static void main(String[] args) throws Exception {
        OS.Startup(new HelloWorld());
        OS.CreateProcess(new Ping());
        OS.CreateProcess(new Pong());
        OS.CreateProcess(new GoodbyeWorld());
    }
}