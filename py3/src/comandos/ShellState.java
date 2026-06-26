package comandos;

public class ShellState {
    public int currentDirId;
    public int currentUserId;
    public String username;

    public ShellState() {
        this.currentDirId  = 0;      // arranca en la raíz
        this.currentUserId = 0;      // arranca como root
        this.username      = "root";
    }
}