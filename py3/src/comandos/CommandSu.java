package comandos;

import fileSystem.FileSystem;
import nucleo.UserEntry;
import java.util.Scanner;

public class CommandSu implements Command {

    private FileSystem fs;
    private ShellState state;
    private Scanner scanner;

    public CommandSu(FileSystem fs, ShellState state, Scanner scanner) {
        this.fs      = fs;
        this.state   = state;
        this.scanner = scanner;
    }

    @Override
    public void execute(String[] args) {

        // Determinar a qué usuario se quiere cambiar
        // si no se especifica username → root
        String targetUsername = (args.length >= 2) ? args[1] : "root";

        // Buscar el usuario en la tabla
        UserEntry targetUser = buscarUsuario(targetUsername);

        if (targetUser == null) {
            System.out.println("Error: el usuario '" + targetUsername + "' no existe");
            return;
        }

        if (!targetUser.active) {
            System.out.println("Error: el usuario '" + targetUsername + "' está desactivado");
            return;
        }

        // Pedir contraseña
        System.out.print("Contraseña: ");
        String password = scanner.nextLine();

        if (!targetUser.checkPassword(password)) {
            System.out.println("Error: contraseña incorrecta");
            return;
        }

        // Cambiar el estado de la sesión
        state.currentUserId = targetUser.userId;
        state.username      = targetUser.username;

        // Cambiar al directorio home del usuario
        int homeId = buscarHome(targetUser);
        if (homeId != -1) {
            state.currentDirId = homeId;
        }

        System.out.println("Sesión iniciada como " + targetUser.username);
    }

    // Busca un usuario por username en la tabla
    private UserEntry buscarUsuario(String username) {
        for (int i = 0; i < fs.userCount; i++) {
            if (fs.userTable[i] != null && fs.userTable[i].username.equals(username)) {
                return fs.userTable[i];
            }
        }
        return null;
    }

    // Busca el inodo del home del usuario recorriendo el árbol
    // root    → inodo con nombre "root" hijo de "/"
    // otros   → inodo con nombre username hijo de "/home"
    private int buscarHome(UserEntry user) {
        if (user.isRoot) {
            // /root está directamente bajo la raíz (inodo 0)
            return fs.buscarInodePorNombre("root", 0);
        } else {
            // /home/username → primero buscar /home bajo la raíz
            int homeId = fs.buscarInodePorNombre("home", 0);
            if (homeId == -1) return -1;
            return fs.buscarInodePorNombre(user.username, homeId);
        }
    }
}