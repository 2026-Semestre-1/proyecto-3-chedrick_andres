package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;
import java.io.IOException;

public class CatCommand implements Command {

    private FileSystem fs;
    private ShellState state;

    public CatCommand(FileSystem fs, ShellState state) {
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: cat nombre");
            return;
        }

        int inodeId = fs.buscarInodePorNombre(args[1], state.currentDirId);
        if (inodeId == -1) {
            System.out.println("Error: '" + args[1] + "' no existe");
            return;
        }

        try {
            Inode inode = fs.inodeTable[inodeId];

            if (inode == null || !inode.type.equals(Inode.FILE)) {
                System.out.println("Error: no es un archivo válido");
                return;
            }

            int[] blocks = inode.getUsedBlocks();
            if (blocks.length == 0) {
                System.out.println("(archivo vacío)");
                return;
            }

            StringBuilder contenido = new StringBuilder();
            int blockSize = fs.superblock.blockSize;
            int bytesLeidos = 0;

            for (int blockNum : blocks) {
                byte[] data = fs.diskManager.readBlock(blockNum, fs.superblock.dataBlocksOffset, blockSize);
                int bytesEnEsteBloque = Math.min(blockSize, inode.sizeBytes - bytesLeidos);
                contenido.append(new String(data, 0, bytesEnEsteBloque));
                bytesLeidos += bytesEnEsteBloque;
            }

            System.out.println(contenido.toString());

        } catch (IOException e) {
            System.out.println("Error al leer archivo: " + e.getMessage());
        }
    }
}