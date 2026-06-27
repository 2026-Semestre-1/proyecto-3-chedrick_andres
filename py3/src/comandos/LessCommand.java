package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;
import java.io.IOException;
import java.util.Scanner;

public class LessCommand implements Command {

    private FileSystem fs;
    private ShellState state;
    private Scanner scanner;

    public LessCommand(FileSystem fs, ShellState state, Scanner scanner) {
        this.fs = fs;
        this.state = state;
        this.scanner = scanner;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: less nombre");
            return;
        }

        int inodeId = fs.buscarInodePorNombre(args[1], state.currentDirId);
        if (inodeId == -1) {
            System.out.println("Error: '" + args[1] + "' no existe");
            return;
        }

        Inode inode = fs.inodeTable[inodeId];

        if (!inode.type.equals(Inode.FILE)) {
            System.out.println("Error: '" + args[1] + "' no es un archivo");
            return;
        }

        int ownerId = state.currentUserId;
        int groupId = buscarGroupId(ownerId);

        if (!inode.canRead(ownerId, groupId)) {
            System.out.println("Error: no tiene permisos de lectura sobre '" + args[1] + "'");
            return;
        }

        inode.status = Inode.OPEN;

        try {
            String contenido = leerContenido(inode);
            System.out.println(contenido);
            System.out.println();
            System.out.println("(escriba 'q' para salir)");

            boolean abierto = true;
            while (abierto) {
                String linea = scanner.nextLine();
                if (linea.trim().equals("q")) {
                    abierto = false;
                }
            }

        } catch (IOException ex) {
            System.getLogger(LessCommand.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        } finally {
            inode.status = Inode.CLOSED;
            try {
                fs.diskManager.writeInode(inode, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            } catch (IOException e) {
                System.out.println("Error al cerrar archivo: " + e.getMessage());
            }
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

    private String leerContenido(Inode inode) throws IOException {
        int[] blocks = inode.getUsedBlocks();
        if (blocks.length == 0) return "(archivo vacío)";

        StringBuilder contenido = new StringBuilder();
        int blockSize = fs.superblock.blockSize;
        int bytesLeidos = 0;

        for (int blockNum : blocks) {
            byte[] data = fs.diskManager.readBlock(blockNum, fs.superblock.dataBlocksOffset, blockSize);
            int bytesEnEsteBloque = Math.min(blockSize, inode.sizeBytes - bytesLeidos);
            contenido.append(new String(data, 0, bytesEnEsteBloque));
            bytesLeidos += bytesEnEsteBloque;
        }
        return contenido.toString();
    }
}