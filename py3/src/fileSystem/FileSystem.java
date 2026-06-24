/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fileSystem;

import administradorDisco.DiskManager;
import java.io.IOException;
import java.util.Scanner;
import nucleo.Inode;
import nucleo.MapaDeBits;
import nucleo.SuperBlock;
import nucleo.UserEntry;

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

    public UserEntry[] userTable;
    public int userCount = 0;

    public FileSystem() {
        userTable = new UserEntry[UserEntry.MAX_USERS];
    }

    public void format(int diskMB, int blockSize, String filename) {
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

            // Crear usuario root en la tabla de usuarios
            UserEntry rootUser = new UserEntry();
            rootUser.init(0, "root", "Root User", password, 0, true);
            userTable[0] = rootUser;
            userCount++;
            diskManager.writeUser(rootUser, superblock.userTableOffset, UserEntry.USER_SIZE);

            // Crear inodo raíz "/"
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
            superblock.print();

        } catch (IOException e) {
            System.out.println("Error al formatear el disco: " + e.getMessage());
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