/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;
import java.io.IOException;

/**
 *
 * @author joses
 */
public class DirCommands {

    private FileSystem fs;

    public DirCommands(FileSystem fs) {
        this.fs = fs;
    }
    //makdir
    public void mkdir(String name, int parentId, int ownerId, int groupId) {
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
   // ===== Comando ls =====
    public void ls(int dirId, boolean recursive) {
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

    // ls sin -R: solo el nivel actual
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

    // ls -R: entra a cada subdirectorio también
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
