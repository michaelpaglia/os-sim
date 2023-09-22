public class GoodbyeWorld extends UserlandProcess {
    @Override
    public void run() {
        while (true) { // infinite loop as per assignment requirements
            //System.out.println(" ");
            System.out.println("Goodbye World");
            //System.out.println(" ");
            OS.Sleep(250);
        }
    }
}
