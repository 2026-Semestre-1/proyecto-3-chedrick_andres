/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;
import java.io.IOException;

/**
 *
 * @author joses
 */
public class MkdirCommand implements Command {

    private FileSystem fs;
    private ShellState state;

    public MkdirCommand(FileSystem fs, ShellState state) {
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: mkdir nombre");
            return;
        }

        String name = args[1];
        int parentId = state.currentDirId;
        int ownerId = state.currentUserId;
        int groupId = state.currentGroupId;

        try {
            if (fs.superblock.freeInodes <= 0) {
                System.out.println("Error: no hay espacio para más archivos/directorios");
                return;
            }

            Inode parent = fs.inodeTable[parentId];
            if (parent == null || !parent.type.equals(Inode.DIR)) {
                System.out.println("Error: el directorio padre no existe o no es válido");
                return;
            }
            if (state.currentUserId != 0 && !parent.canWrite(state.currentUserId, state.currentGroupId)) {
                System.out.println("Error: no tienes permisos para crear directorios aquí");
                return;
            }

            for (int i = 0; i < parent.childCount; i++) {
                Inode child = fs.inodeTable[parent.children[i]];
                if (child != null && child.getFullName().equals(name)) {
                    System.out.println("Error: ya existe '" + name + "' en este directorio");
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

            Inode nuevo = new Inode();
            nuevo.init(newId, name, Inode.DIR, ownerId, groupId, parentId);
            nuevo.ownerPerm = 7;
            nuevo.groupPerm = 5;

            parent.addChild(newId);
            fs.inodeTable[newId] = nuevo;

            fs.superblock.inodeUsed();

            fs.diskManager.writeInode(nuevo, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeInode(parent, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeSuperBlock(fs.superblock);

            System.out.println("Directorio '" + name + "' creado correctamente");

        } catch (IOException e) {
            System.out.println("Error al crear directorio: " + e.getMessage());
        }
    }
}