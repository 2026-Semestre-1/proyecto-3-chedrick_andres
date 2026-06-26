
package comandos;

import fileSystem.FileSystem;
import nucleo.User;
import java.io.IOException;
import java.util.Scanner;

public class CommandPasswd implements Command {

    private FileSystem fs;
    private ShellState state;
    private Scanner scanner;

    public CommandPasswd(FileSystem fs, ShellState state, Scanner scanner) {
        this.fs      = fs;
        this.state   = state;
        this.scanner = scanner;
    }

    @Override
    public void execute(String[] args) {

        // 1. Validar argumentos
        if (args.length < 2) {
            System.out.println("Uso: passwd username");
            return;
        }

        String targetUsername = args[1];

        // 2. Buscar el usuario en la tabla
        User targetUser = buscarUsuario(targetUsername);

        if (targetUser == null) {
            System.out.println("Error: el usuario '" + targetUsername + "' no existe");
            return;
        }

        // 3. Verificar permisos:
        //    root puede cambiar cualquier contraseña
        //    un usuario normal solo puede cambiar la suya
        boolean esRoot = (state.currentUserId == 0);
        boolean esSuPropia = (targetUser.userId == state.currentUserId);

        if (!esRoot && !esSuPropia) {
            System.out.println("Error: no tienes permisos para cambiar la contraseña de otro usuario");
            return;
        }

        // 4. Pedir nueva contraseña y confirmación
        System.out.print("password: ");
        String newPassword = scanner.nextLine();

        System.out.print("confirm password: ");
        String confirm = scanner.nextLine();

        if (newPassword.isEmpty()) {
            System.out.println("Error: la contraseña no puede estar vacía");
            return;
        }

        if (!newPassword.equals(confirm)) {
            System.out.println("Error: las contraseñas no coinciden");
            return;
        }

        // 5. Actualizar el hash en memoria
        targetUser.passwordHash = User.hashPassword(newPassword);

        // 6. Persistir en disco
        try {
            fs.diskManager.writeUser(targetUser, fs.superblock.userTableOffset, User.USER_SIZE);
            System.out.println("Contraseña de '" + targetUsername + "' actualizada correctamente");
        } catch (IOException e) {
            System.out.println("Error al guardar la contraseña en disco: " + e.getMessage());
        }
    }

    // Busca un usuario por username en la tabla
    private User buscarUsuario(String username) {
        for (int i = 0; i < fs.userCount; i++) {
            if (fs.userTable[i] != null && fs.userTable[i].username.equals(username)) {
                return fs.userTable[i];
            }
        }
        return null;
    }
}