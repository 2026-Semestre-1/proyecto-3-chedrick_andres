/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comandos;

import fileSystem.FileSystem;
import java.io.IOException;
import nucleo.Inode;

/**
 *
 * @author joses
 */
public class FileCommands {

    private FileSystem fs;

    public FileCommands(FileSystem fs) {
        this.fs = fs;
    }

    //TOUCH
    public void touch(String name, int parentId, int ownerId, int groupId) {
        try {
            //  un inodo nuevo
            if (fs.superblock.freeInodes <= 0) {
                System.out.println("Error: no hay espacio para más archivos");
                return;
            }

            //  Si padre existe y es un directorio
            Inode parent = fs.inodeTable[parentId];
            if (parent == null || !parent.type.equals(Inode.DIR)) {
                System.out.println("Error: el directorio no existe o no es válido");
                return;
            }

            // Verifica que no exista ya un archivo con ese nombre
            for (int i = 0; i < parent.childCount; i++) {
                Inode child = fs.inodeTable[parent.children[i]];
                if (child != null && child.getFullName().equals(name)) {
                    System.out.println("Error: ya existe '" + name + "' en este directorio");
                    return;
                }
            }

            // Verificamj que haya al menos 1 bloque libre 
            int needed = fs.bitmap.blocksNeeded(0, fs.superblock.blockSize);
            if (needed == 0) {
                needed = 1;
            }

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
                fs.bitmap.free(blocks); // liberamos lo que habíamos reservado
                return;
            }

            // Inode tipo FILE
            Inode nuevo = new Inode();
            nuevo.init(newId, name, Inode.FILE, ownerId, groupId, parentId);
            nuevo.ownerPerm = 6; // rw-
            nuevo.groupPerm = 4; // r--
            nuevo.setBlocks(blocks);
            nuevo.sizeBytes = 0; // vacío por ahora

            //  hijo del padre
            parent.addChild(newId);
            fs.inodeTable[newId] = nuevo;

            //  Actualiza contadores
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
