package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;

public class LsCommand implements Command {

    private FileSystem fs;
    private ShellState state;

    public LsCommand(FileSystem fs, ShellState state) {
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        boolean recursive = args.length > 1 && args[1].equals("-R");
        int dirId = state.currentDirId;

        Inode dir = fs.inodeTable[dirId];
        if (dir == null || !dir.type.equals(Inode.DIR)) {
            System.out.println("Error: directorio no válido");
            return;
        }

        if (recursive) {
            lsRecursivo(dirId, 0);
        } else {
            lsSimple(dirId);
        }
    }

    private void lsSimple(int dirId) {
        Inode dir = fs.inodeTable[dirId];

        if (dir.childCount == 0) {
            System.out.println("(directorio vacío)");
            return;
        }

        for (int i = 0; i < dir.childCount; i++) {
            Inode child = fs.inodeTable[dir.children[i]];
            if (child == null) continue;

            String tipo = child.type.equals(Inode.DIR) ? "[D]" : "[F]";
            System.out.println(tipo + "  " + child.getFullName());
        }
    }

    private void lsRecursivo(int dirId, int depth) {
        Inode dir = fs.inodeTable[dirId];
        String indent = "  ".repeat(depth);

        for (int i = 0; i < dir.childCount; i++) {
            Inode child = fs.inodeTable[dir.children[i]];
            if (child == null) continue;

            String tipo = child.type.equals(Inode.DIR) ? "[D]" : "[F]";
            System.out.println(indent + tipo + "  " + child.getFullName());

            if (child.type.equals(Inode.DIR)) {
                lsRecursivo(child.id, depth + 1);
            }
        }
    }
}