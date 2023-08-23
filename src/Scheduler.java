import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    LinkedList<KernelandProcess> kernelandProcessList;
    Timer timer;
    KernelandProcess currentKernelandProcess;

    public Scheduler() {
        kernelandProcessList = new LinkedList<>();
        timer = new Timer();
        timer.schedule(new Interrupt(), 250);
    }

    public int CreateProcess(UserlandProcess up) {
        KernelandProcess newProcess = new KernelandProcess(up);
        kernelandProcessList.add(newProcess);
        if (currentKernelandProcess == null) {
            SwitchProcess();
        }
        return newProcess.getPid();
    }
    public void SwitchProcess() {
        if (currentKernelandProcess != null) {
            if (!currentKernelandProcess.isDone()) {
                kernelandProcessList.add(currentKernelandProcess);
            }
            currentKernelandProcess.stop();
        }
        if (!kernelandProcessList.isEmpty()) {
            KernelandProcess firstItemInList = kernelandProcessList.removeFirst();
            firstItemInList.run();
        }
    }
    private class Interrupt extends TimerTask {
        @Override
        public void run() {
            SwitchProcess();
        }
    }
}
