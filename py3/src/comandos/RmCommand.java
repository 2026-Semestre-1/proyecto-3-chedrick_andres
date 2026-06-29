package comandos;
import fileSystem.FileSystem;
import nucleo.Inode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
            System.out.println("Uso: rm nombre [-R] / rm *.txt / rm *");
            return;
        }

        // Detectar -R en cualquier posición
        boolean recursivo = false;
        String patron = null;

        for (int i = 1; i < args.length; i++) {
            if (args[i].equals("-R")) {
                recursivo = true;
            } else {
                patron = args[i];
            }
        }

        if (patron == null) {
            System.out.println("Uso: rm nombre [-R]");
            return;
        }

        // Buscar los inodos que coinciden con el patrón
        List<Integer> aEliminar = buscarPorPatron(patron, state.currentDirId);

        if (aEliminar.isEmpty()) {
            System.out.println("Error: no se encontró ningún archivo que coincida con '" + patron + "'");
            return;
        }

        for (int inodeId : aEliminar) {
            Inode target = fs.inodeTable[inodeId];
            if (target != null) {
                rm(target.getFullName(), state.currentDirId, recursivo);
            }
        }
    }

    private List<Integer> buscarPorPatron(String patron, int dirId) {
        List<Integer> resultado = new ArrayList<>();
        Inode dir = fs.inodeTable[dirId];
        if (dir == null) return resultado;

        for (int i = 0; i < dir.childCount; i++) {
            int childId = dir.children[i];
            Inode child = fs.inodeTable[childId];
            if (child == null) continue;

            String nombre = child.getFullName();

            if (coincide(nombre, patron)) {
                resultado.add(childId);
            }
        }
        return resultado;
    }

    private boolean coincide(String nombre, String patron) {
        if (patron.equals("*")) {
            return true; // todo
        }
        if (patron.startsWith("*.")) {
            // cualquier cosa que termine en .txt
            String ext = patron.substring(1); // ".txt"
            return nombre.endsWith(ext);
        }
        if (patron.startsWith(".")) {
            // igual que *.txt
            return nombre.endsWith(patron);
        }
        // nombre exacto
        return nombre.equals(patron);
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

            if (state.currentUserId != 0 && !target.canWrite(state.currentUserId, state.currentGroupId)) {
                System.out.println("Error: no tenés permisos para eliminar '" + name + "'");
                return;
            }

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