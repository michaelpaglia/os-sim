public class KernelMessage {
    int senderPid, targetPid, whatMessage;
    byte[] data;
    public KernelMessage(int p_senderPid, int p_targetPid, int p_whatMessage, byte[] p_data) {
        this.senderPid = p_senderPid;
        this.targetPid = p_targetPid;
        this.whatMessage = p_whatMessage++;
        this.data = p_data;
    }
    public KernelMessage(KernelMessage km) {
        this.senderPid = km.senderPid;
        this.targetPid = km.targetPid;
        this.whatMessage = km.whatMessage++;
        this.data = km.data;
    }
    public String toString() {
        return "Sender PID: " + this.senderPid + ". Target PID: " + this.targetPid + ". What: " + this.whatMessage + ".";
    }
}
