package shell;
import fileSystem.FileSystem;
import comandos.CommandFactory;
import comandos.ShellState;
import comandos.Command;
import java.io.IOException;
import java.util.Scanner;

public class Shell {
    private FileSystem fs;
    private Scanner scanner;
    private ShellState state;
    private CommandFactory factory;

    public Shell(FileSystem fs) {
        this.fs = fs;
        this.scanner = new Scanner(System.in);
        this.state = new ShellState();
        this.factory = new CommandFactory(fs, state, scanner);
    }

    public void run() {
        System.out.println("Bienvenido a miFS. Escriba 'exit' para salir.");
        boolean corriendo = true;
        while (corriendo) {
            System.out.print(state.username + "@miFS: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;
            String[] partes = input.split("\\s+");
            String nombreComando = partes[0];

            if (nombreComando.equals("exit")) {
                System.out.println("Cerrando miFS...");
                try {
                    fs.diskManager.closeDisk();
                } catch (IOException e) {
                    System.out.println("Error al cerrar el disco: " + e.getMessage());
                }
                corriendo = false;
                continue;
            }

            Command comando = factory.getCommand(nombreComando);
            if (comando == null) {
                System.out.println("Comando no reconocido: " + nombreComando);
            } else {
                comando.execute(partes);
            }
        }
    }
}