package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;
import java.io.IOException;

public class RmCommand implements Command {

    private FileSystem fs;
    private ShellState state;

    public RmCommand(FileSystem fs, ShellState state) {
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: rm nombre [-R]");
            return;
        }
        boolean recursivo = args.length > 2 && args[2].equals("-R");
        rm(args[1], state.currentDirId, recursivo);
    }

    private void rm(String name, int dirId, boolean recursivo) {
        try {
            int targetId = fs.buscarInodePorNombre(name, dirId);

            if (targetId == -1) {
                System.out.println("Error: '" + name + "' no existe");
                return;
            }

            Inode target = fs.inodeTable[targetId];
            Inode parent = fs.inodeTable[dirId];

            if (target.type.equals(Inode.DIR) && target.childCount > 0 && !recursivo) {
                System.out.println("Error: '" + name + "' no está vacío. Use -R para borrar recursivamente");
                return;
            }

            if (target.type.equals(Inode.DIR) && recursivo) {
                int[] hijosCopia = target.children.clone();
                int cantidadHijos = target.childCount;

                for (int i = 0; i < cantidadHijos; i++) {
                    Inode hijo = fs.inodeTable[hijosCopia[i]];
                    if (hijo != null) {
                        rm(hijo.getFullName(), targetId, true);
                    }
                }
            }

            if (target.type.equals(Inode.FILE)) {
                int[] blocks = target.getUsedBlocks();
                fs.bitmap.free(blocks);
                fs.superblock.blockFreed(blocks.length);
            }

            parent.removeChild(targetId);
            fs.inodeTable[targetId] = null;
            fs.superblock.inodeFreed();

            fs.diskManager.writeBitmap(fs.bitmap, fs.superblock.bitmapOffset, fs.superblock.bitmapMaxSize);
            fs.diskManager.writeInode(parent, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeSuperBlock(fs.superblock);

            System.out.println("'" + name + "' eliminado correctamente");

        } catch (IOException e) {
            System.out.println("Error al eliminar: " + e.getMessage());
        }
    }
}