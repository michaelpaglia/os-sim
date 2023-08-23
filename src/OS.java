public class OS {
    private static Kernel pKernel;

    public static void Startup(UserlandProcess init) {
        pKernel = new Kernel();
        CreateProcess(init);
    }
    public static int CreateProcess(UserlandProcess up) {
        return pKernel.CreateProcess(up);
    }
}
