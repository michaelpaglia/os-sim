public class GoodbyeWorld extends UserlandProcess {
    @Override
    public void run() {
        while (true) { // infinite loop as per assignment requirements
            System.out.println("Goodbye World");
            OS.Sleep(50);
        }
    }
}
