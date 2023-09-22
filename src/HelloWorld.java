public class HelloWorld extends UserlandProcess {
    @Override
    public void run() {
        while (true) { // infinite loop as per assignment requirements
            //System.out.println(" ");
            System.out.println("Hello World");
            //System.out.println(" ");
            OS.Sleep(250);
        }
    }
}
