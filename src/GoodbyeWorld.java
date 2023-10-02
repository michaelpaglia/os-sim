public class GoodbyeWorld extends UserlandProcess {
    @Override
    public void run() {
        while (true) { // infinite loop as per assignment requirements
            System.out.println("Goodbye World");
            int openfd = OS.Open("random 100");
            if (openfd != -1) {
                OS.Read(openfd, 10);
                //OS.Close(openfd);
            }
            OS.Sleep(50);
        }
    }
}
