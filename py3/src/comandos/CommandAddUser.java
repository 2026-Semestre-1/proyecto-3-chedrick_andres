package comandos;

import fileSystem.FileSystem;
import java.io.IOException;
import java.util.Scanner;
import nucleo.Inode;
import nucleo.UserEntry;

public class CommandAddUser implements Command {

    private FileSystem fs;
    private ShellState state;
    private Scanner scanner;

    public CommandAddUser(FileSystem fs, ShellState state, Scanner scanner) {
        this.fs      = fs;
        this.state   = state;
        this.scanner = scanner;
    }

    @Override
    public void execute(String[] args) {

        // 1. Validar argumentos
        if (args.length < 2) {
            System.out.println("Uso: useradd username");
            return;
        }

        // 2. Solo root puede crear usuarios
        if (state.currentUserId != 0) {
            System.out.println("Error: solo root puede crear usuarios");
            return;
        }

        String username = args[1];

        // 3. Verificar límite de usuarios
        if (fs.userCount >= UserEntry.MAX_USERS) {
            System.out.println("Error: se alcanzó el límite máximo de usuarios (" + UserEntry.MAX_USERS + ")");
            return;
        }

        // 4. Verificar que el username no exista ya
        for (int i = 0; i < fs.userCount; i++) {
            if (fs.userTable[i] != null && fs.userTable[i].username.equals(username)) {
                System.out.println("Error: el usuario '" + username + "' ya existe");
                return;
            }
        }

        // 5. Pedir nombre completo
        System.out.print("Nombre completo: ");
        String fullName = scanner.nextLine().trim();

        if (fullName.isEmpty()) {
            System.out.println("Error: el nombre completo no puede estar vacío");
            return;
        }

        // 6. Pedir contraseña y confirmación
        System.out.print("Contraseña: ");
        String password = scanner.nextLine();

        System.out.print("Confirme contraseña: ");
        String confirm = scanner.nextLine();

        if (password.isEmpty()) {
            System.out.println("Error: la contraseña no puede estar vacía");
            return;
        }

        if (!password.equals(confirm)) {
            System.out.println("Error: las contraseñas no coinciden");
            return;
        }

        // 7. Asignar userId (el siguiente disponible)
        int newUserId = fs.userCount;

        // 8. Crear el UserEntry
        UserEntry newUser = new UserEntry();
        newUser.init(newUserId, username, fullName, password, newUserId, false);

        // 9. Buscar o crear /home, luego crear /home/username
        int homeParentId = getOrCreateHomeDir();
        if (homeParentId == -1) {
            System.out.println("Error: no se pudo encontrar o crear el directorio /home");
            return;
        }

        crearDirectorio(username, homeParentId, newUserId);

        // 10. Guardar en tabla en memoria
        fs.userTable[newUserId] = newUser;
        fs.userCount++;

        // 11. Persistir en disco
        try {
            fs.diskManager.writeUser(newUser, fs.superblock.userTableOffset, UserEntry.USER_SIZE);
            fs.diskManager.writeSuperBlock(fs.superblock);
            System.out.println("Usuario '" + username + "' creado correctamente");
            System.out.println("Home: " + newUser.homePath);
        } catch (IOException e) {
            System.out.println("Error al guardar usuario en disco: " + e.getMessage());
        }
    }

    // Busca /home entre los hijos de la raíz.
    // Si no existe, lo crea. Retorna su inodeId o -1 si falla.
    private int getOrCreateHomeDir() {
        Inode root = fs.inodeTable[0];
        if (root == null) return -1;

        for (int i = 0; i < root.childCount; i++) {
            Inode child = fs.inodeTable[root.children[i]];
            if (child != null && child.name.equals("home") && child.type.equals(Inode.DIR)) {
                return child.id;
            }
        }

        // No existe → crear /home con dueño root
        System.out.println("(Creando directorio /home...)");
        crearDirectorio("home", 0, 0);

        // Buscarlo de nuevo tras crearlo
        for (int i = 0; i < root.childCount; i++) {
            Inode child = fs.inodeTable[root.children[i]];
            if (child != null && child.name.equals("home")) {
                return child.id;
            }
        }

        return -1;
    }

    // Misma lógica que MkdirCommand pero recibe parentId y ownerId directamente
    // porque no depende del directorio actual del shell
    private void crearDirectorio(String name, int parentId, int ownerId) {
        try {
            Inode parent = fs.inodeTable[parentId];
            if (parent == null || !parent.type.equals(Inode.DIR)) return;

            // Buscar slot libre en la tabla de inodos
            int newId = -1;
            for (int i = 0; i < fs.inodeTable.length; i++) {
                if (fs.inodeTable[i] == null) {
                    newId = i;
                    break;
                }
            }

            if (newId == -1) {
                System.out.println("Error: tabla de inodos llena");
                return;
            }

            Inode nuevo = new Inode();
            nuevo.init(newId, name, Inode.DIR, ownerId, ownerId, parentId);
            nuevo.ownerPerm = 7;
            nuevo.groupPerm = 0; // solo el dueño accede a su home

            parent.addChild(newId);
            fs.inodeTable[newId] = nuevo;
            fs.superblock.inodeUsed();

            fs.diskManager.writeInode(nuevo, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeInode(parent, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeSuperBlock(fs.superblock);

        } catch (IOException e) {
            System.out.println("Error al crear directorio: " + e.getMessage());
        }
    }
}