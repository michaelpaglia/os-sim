import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class Scheduler {
    LinkedList<KernelandProcess> kernelandProcessList; // holds list of processes for scheduler
    Timer timer;
    KernelandProcess currentKernelandProcess; // reference to KernelandProcess currently running

    /**
     * Constructs a scheduler which holds a list of processes, a timer instance, and a scheduled interrupt every 250ms
     */
    public Scheduler() {
        kernelandProcessList = new LinkedList<>();
        timer = new Timer();
        timer.schedule(new Interrupt(), 250);
    }

    /**
     * Constructs a KernelandProcess, adds it to the list of processes, and calls SwitchProcess() if not running
     * @param up A certain UserlandProcess to start
     * @return PID of new process
     */
    public int CreateProcess(UserlandProcess up) {
        KernelandProcess newProcess = new KernelandProcess(up);
        kernelandProcessList.add(newProcess);
        if (currentKernelandProcess == null) {
            SwitchProcess();
        }
        return newProcess.getPid();
    }

    /**
     * If something is running, process stops; if process didn't finish, adds to list of processes
     */
    public void SwitchProcess() {
        if (currentKernelandProcess != null) { // process is running
            if (!currentKernelandProcess.isDone()) {
                kernelandProcessList.add(currentKernelandProcess);
            }
            currentKernelandProcess.stop();
        }
        if (!kernelandProcessList.isEmpty()) { // prevent null-pointer exception
            KernelandProcess firstItemInList = kernelandProcessList.removeFirst();
            firstItemInList.run();
        }
    }

    /**
     * Used to schedule an interrupt in Scheduler constructor
     */
    private class Interrupt extends TimerTask {
        @Override
        public void run() {
            SwitchProcess();
        }
    }
}
