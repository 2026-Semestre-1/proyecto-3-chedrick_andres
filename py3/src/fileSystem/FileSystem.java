/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fileSystem;
import nucleo.SuperBlock;
import nucleo.MapaDeBits;
import nucleo.Inode;
import administradorDisco.DiskManager;
import comandos.DirCommands;
import java.io.IOException;
import java.util.Scanner;
import comandos.FileCommands;

/**
 *
 * @author joses
 */

/**
 *
 * @author joses
 */
public class FileSystem {

    public SuperBlock superblock;
    public MapaDeBits bitmap;
    public Inode[] inodeTable;
    public DiskManager diskManager;
    public static final int INODE_SIZE = 1024; // bytes reservados por inodo en disco
    public FileCommands fileCommands;
    public DirCommands dirCommands;
    public FileSystem() {
        dirCommands = new DirCommands(this);
        fileCommands = new FileCommands(this);
         
    }
    
    //Comando format 
    //  format 10 512 miDiscoDuro.fs
    public void format(int diskMB, int blockSize, String filename) {
        try {
            long diskSizeBytes = (long) diskMB * 1024 * 1024;

            System.out.println("---- Ejecutando format--- ");
            System.out.println("Tamaño solicitado: " + diskMB + " MB");
            System.out.println("Tamaño de bloque:  " + blockSize + " bytes");

            //SuperBlock en memoria
            superblock = new SuperBlock();
            superblock.init("miFS", diskSizeBytes, blockSize, filename);

            // todos los bloques libres
            bitmap = new MapaDeBits(superblock.totalBlocks);

            //  tabla de inodos vacia
            inodeTable = new Inode[superblock.maxInodes];

            // archivo físico .fs
            diskManager = new DiskManager(filename);
            diskManager.createDisk(diskSizeBytes);
            diskManager.openDisk();

            // SuperBlock y el Bitmap al disco
            diskManager.writeSuperBlock(superblock);
            diskManager.writeBitmap(bitmap, superblock.bitmapOffset);

            // ->  Le pedimos password de root y lo crea
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
            root.init(0, "/", Inode.DIR, 0, 0, -1); // id=0, owner=root(0), grupo root(0), sin padre
            root.ownerPerm = 7;
            root.groupPerm = 5;
            inodeTable[0] = root;

            Inode homeRoot = new Inode();
            homeRoot.init(1, "root", Inode.DIR, 0, 0, 0); // id=1, padre=0 (la raíz)
            homeRoot.ownerPerm = 7;
            homeRoot.groupPerm = 0;
            inodeTable[1] = homeRoot;
            root.addChild(1);

            superblock.inodeUsed(); // por la raíz
            superblock.inodeUsed(); // por /root

         
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
    
     //Comnados
    public void mkdir(String name, int parentId, int ownerId, int groupId) {
        dirCommands.mkdir(name, parentId, ownerId, groupId);
    }
    
    public void touch(String name, int parentId, int ownerId, int groupId) {
        fileCommands.touch(name, parentId, ownerId, groupId);
    }
    
    public void ls(int dirId, boolean recursive) {
        dirCommands.ls(dirId, recursive);
    }
     
}
