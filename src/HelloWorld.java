public class HelloWorld extends UserlandProcess {
    @Override
    public void run() {
        while (true) { // infinite loop as per assignment requirements
            System.out.println("Hello World");
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }
    }
}
