public class Main {
    public static void main(String[] args) {
        UserlandProcess hello = new HelloWorld();
        UserlandProcess goodbye = new GoodbyeWorld();
        OS.Startup(hello);
        OS.CreateProcess(goodbye);
    }
}