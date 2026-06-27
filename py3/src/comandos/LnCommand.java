package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;
import java.io.IOException;

public class LnCommand implements Command {

    private FileSystem fs;
    private ShellState state;

    public LnCommand(FileSystem fs, ShellState state) {
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: ln nombre_enlace ruta_archivo_destino");
            return;
        }

        String nombreEnlace = args[1];
        String rutaDestino = args[2];

        int destinoId = resolverRuta(rutaDestino);

        if (destinoId == -1) {
            System.out.println("Error: el archivo destino '" + rutaDestino + "' no existe");
            return;
        }

        Inode destino = fs.inodeTable[destinoId];

        int ownerId = state.currentUserId;
        int groupId = buscarGroupId(ownerId);

        if (!destino.canRead(ownerId, groupId)) {
            System.out.println("Error: no tiene permisos sobre '" + rutaDestino + "'");
            return;
        }

        try {
            int parentId = state.currentDirId;
            Inode parent = fs.inodeTable[parentId];

            for (int i = 0; i < parent.childCount; i++) {
                Inode child = fs.inodeTable[parent.children[i]];
                if (child != null && child.getFullName().equals(nombreEnlace)) {
                    System.out.println("Error: ya existe '" + nombreEnlace + "' en este directorio");
                    return;
                }
            }

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

            Inode enlace = new Inode();
            enlace.init(newId, nombreEnlace, Inode.FILE, ownerId, groupId, parentId);
            enlace.ownerPerm = 7;
            enlace.groupPerm = 5;
            enlace.linkTarget = rutaDestino;
            enlace.sizeBytes = 0;

            parent.addChild(newId);
            fs.inodeTable[newId] = enlace;
            fs.superblock.inodeUsed();

            fs.diskManager.writeInode(enlace, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeInode(parent, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeSuperBlock(fs.superblock);

            System.out.println("Enlace '" + nombreEnlace + "' creado correctamente -> " + rutaDestino);

        } catch (IOException e) {
            System.out.println("Error al crear enlace: " + e.getMessage());
        }
    }

    private int buscarGroupId(int userId) {
        for (int i = 0; i < fs.userTable.length; i++) {
            if (fs.userTable[i] != null && fs.userTable[i].userId == userId) {
                return fs.userTable[i].groupId;
            }
        }
        return -1;
    }

    private int resolverRuta(String ruta) {
        if (ruta.startsWith("/")) {
            String[] partes = ruta.split("/");
            int currentId = 0;
            for (String parte : partes) {
                if (parte.isEmpty()) continue;
                currentId = fs.buscarInodePorNombre(parte, currentId);
                if (currentId == -1) return -1;
            }
            return currentId;
        } else {
            return fs.buscarInodePorNombre(ruta, state.currentDirId);
        }
    }
}