public class HelloWorld extends UserlandProcess {
    @Override
    public void run() {
        while (true) { // infinite loop as per assignment requirements
            System.out.println("Hello World");
            //int openfd = OS.Open("random 50");

            int openfd = OS.Open("file data.dat");
            if (openfd != -1) {
                OS.Write(openfd, new byte[10]);
                OS.Read(openfd, 10);
                //OS.Close(openfd);
            }
            OS.Sleep(50);
        }
    }
}
