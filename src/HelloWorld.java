public class HelloWorld extends UserlandProcess {
    @Override
    public void run() {
        while (true) { // infinite loop as per assignment requirements
            System.out.println("Hello World");
            try {
                OS.Sleep(1250);
            } catch (Exception e) {
            }
        }
    }
}
