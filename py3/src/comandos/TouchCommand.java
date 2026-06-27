package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;
import java.io.IOException;

public class TouchCommand implements Command {

    private FileSystem fs;
    private ShellState state;

    public TouchCommand(FileSystem fs, ShellState state) {
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: touch nombre");
            return;
        }

        String name = args[1];
        int parentId = state.currentDirId;
        int ownerId = state.currentUserId;
        int groupId = 0;

        try {
            if (fs.superblock.freeInodes <= 0) {
                System.out.println("Error: no hay espacio para más archivos");
                return;
            }

            Inode parent = fs.inodeTable[parentId];
            if (parent == null || !parent.type.equals(Inode.DIR)) {
                System.out.println("Error: el directorio no existe o no es válido");
                return;
            }
            if (state.currentUserId != 0 && !parent.canWrite(state.currentUserId, state.currentGroupId)) {
                System.out.println("Error: no tienes permisos para crear archivos en este directorio");
                return;
            }

            for (int i = 0; i < parent.childCount; i++) {
                Inode child = fs.inodeTable[parent.children[i]];
                if (child != null && child.getFullName().equals(name)) {
                    System.out.println("Error: ya existe '" + name + "' en este directorio");
                    return;
                }
            }

            int needed = fs.bitmap.blocksNeeded(0, fs.superblock.blockSize);
            if (needed == 0) needed = 1;

            int[] blocks = fs.bitmap.allocate(needed);
            if (blocks == null) {
                System.out.println("Error: no hay espacio en disco");
                return;
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
                fs.bitmap.free(blocks);
                return;
            }
            

            Inode nuevo = new Inode();
            nuevo.init(newId, name, Inode.FILE, ownerId, groupId, parentId);
            nuevo.ownerPerm = 6;
            nuevo.groupPerm = 4;
            nuevo.setBlocks(blocks);
            nuevo.sizeBytes = 0;

            parent.addChild(newId);
            fs.inodeTable[newId] = nuevo;

            fs.superblock.inodeUsed();
            fs.superblock.blockUsed(blocks.length);

            fs.diskManager.writeInode(nuevo, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeInode(parent, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeBitmap(fs.bitmap, fs.superblock.bitmapOffset);
            fs.diskManager.writeSuperBlock(fs.superblock);

            System.out.println("Archivo creado");

        } catch (IOException e) {
            System.out.println("Error al crear archivo: " + e.getMessage());
        }
    }
}