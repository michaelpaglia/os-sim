public class Main {
    public static void main(String[] args) {
        //UserlandProcess hello = new HelloWorld();
        //UserlandProcess goodbye = new GoodbyeWorld();
        //OS.Startup(hello);
        //OS.CreateProcess(goodbye);
        OS.Startup(new HelloWorld());
        OS.Sleep(3000);
        OS.CreateProcess(new GoodbyeWorld(), Priority.MEDIUM);
        OS.Sleep(6000);
        OS.CreateProcess(new GoodbyeWorld(), Priority.LOW);
        OS.Sleep(250);
    }
}