public class Main {
    public static void main(String[] args) {
        //UserlandProcess hello = new HelloWorld();
        //UserlandProcess goodbye = new GoodbyeWorld();
        //OS.Startup(hello);
        //OS.CreateProcess(goodbye);
        OS.Startup(new HelloWorld());
        OS.Sleep(5000);

        OS.CreateProcess(new GoodbyeWorld(), Priority.REALTIME);
        OS.Sleep(2500);
    }
}