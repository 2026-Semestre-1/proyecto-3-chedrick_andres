package comandos;

public class ShellState {
    public int currentDirId;
    public String username;

    public ShellState() {
        this.currentDirId = 0; // arranca en la raíz
        this.username = "root"; // temporal, hasta integrar UserManager
    }
}