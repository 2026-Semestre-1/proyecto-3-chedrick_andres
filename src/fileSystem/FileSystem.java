/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fileSystem;

import nucleo.SuperBlock;
import nucleo.MapaDeBits;
import nucleo.Inode;
import administradorDisco.DiskManager;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author joses
 */
public class FileSystem {

    public SuperBlock superblock;
    public MapaDeBits bitmap;
    public Inode[] inodeTable;
    public DiskManager diskManager;
    public static final int INODE_SIZE = 1024;

    public FileSystem() {
    }

    public void format(int diskMB, int blockSize, String filename) throws ClassNotFoundException {
        try {
            long diskSizeBytes = (long) diskMB * 1024 * 1024;

            System.out.println("---- Ejecutando format--- ");
            System.out.println("Tamaño solicitado: " + diskMB + " MB");
            System.out.println("Tamaño de bloque:  " + blockSize + " bytes");

            superblock = new SuperBlock();
            superblock.init("miFS", diskSizeBytes, blockSize, filename);

            bitmap = new MapaDeBits(superblock.totalBlocks);

            inodeTable = new Inode[superblock.maxInodes];

            diskManager = new DiskManager(filename);
            diskManager.createDisk(diskSizeBytes);
            diskManager.openDisk();

            diskManager.writeSuperBlock(superblock);
            diskManager.writeBitmap(bitmap, superblock.bitmapOffset);

            Scanner sc = new Scanner(System.in);
            System.out.print("Defina password para root: ");
            String password = sc.nextLine();
            System.out.print("Confirme password: ");
            String confirm = sc.nextLine();

            if (!password.equals(confirm)) {
                System.out.println("Error: las contraseñas no coinciden");
                return;
            }

            Inode root = new Inode();
            root.init(0, "/", Inode.DIR, 0, 0, -1);
            root.ownerPerm = 7;
            root.groupPerm = 5;
            inodeTable[0] = root;

            Inode homeRoot = new Inode();
            homeRoot.init(1, "root", Inode.DIR, 0, 0, 0);
            homeRoot.ownerPerm = 7;
            homeRoot.groupPerm = 0;
            inodeTable[1] = homeRoot;
            root.addChild(1);

            superblock.inodeUsed();
            superblock.inodeUsed();

            diskManager.writeInode(root, superblock.inodeTableOffset, INODE_SIZE);
            diskManager.writeInode(homeRoot, superblock.inodeTableOffset, INODE_SIZE);

            diskManager.writeSuperBlock(superblock);

            System.out.println();
            System.out.println("Usuario root creado con carpeta HOME en /root");
            System.out.println("Disco formateado exitosamente: " + filename);
            System.out.println("DEBUG: verificando inodo 0 justo antes de salir de format()");
            Inode verificacion = diskManager.readInode(0, superblock.inodeTableOffset, INODE_SIZE);
            System.out.println("DEBUG: verificacion.type = " + verificacion.type);
            
            superblock.print();

        } catch (IOException e) {
            System.out.println("Error al formatear el disco: " + e.getMessage());
        }
    }

   
   public boolean load(String filename) {
        try {
            diskManager = new DiskManager(filename);
            diskManager.openDisk();

            superblock = diskManager.readSuperBlock(2048);

            if (!superblock.isValid()) {
                System.out.println("Error: '" + filename + "' no es un disco válido (firma incorrecta)");
                return false;
            }

            // Le damos margen extra al tamaño de lectura del bitmap ESTO resuelve mi bug de leer tamanos grandes de bits
            int bitmapRawSize = (superblock.totalBlocks / 8) + 1;
            int bitmapReadSize = bitmapRawSize + 200; // margen para overhead de serialización
            bitmap = diskManager.readBitmap(superblock.bitmapOffset, bitmapReadSize);

            inodeTable = diskManager.readAllInodes(superblock.inodeTableOffset, INODE_SIZE, superblock.maxInodes);

            superblock.updateLastMounted();
            diskManager.writeSuperBlock(superblock);

            System.out.println("Disco cargado exitosamente: " + filename);
            superblock.print();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al cargar el disco: " + e.getMessage());
            return false;
        }
    }

    public int buscarInodePorNombre(String name, int dirId) {
        Inode dir = inodeTable[dirId];
        if (dir == null || !dir.type.equals(Inode.DIR)) {
            return -1;
        }

        for (int i = 0; i < dir.childCount; i++) {
            Inode child = inodeTable[dir.children[i]];
            if (child != null && child.getFullName().equals(name)) {
                return child.id;
            }
        }
        return -1;
    }
}
