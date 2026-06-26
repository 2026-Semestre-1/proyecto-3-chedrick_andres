/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nucleo;

import java.io.Serializable;

/**
 *
 * @author joses
 */
public class SuperBlock implements Serializable {

    
// Esto es lo de la firma de MBR
    public static final short MAGIC = (short) 0xAA55;
    public short magic;
    public String filename;  // nombre del archivo .fs "miDiscoDuro.fs"
    
    public String fsName;
    public long createdAt;
    public long lastMounted;

    public long totalSizeBytes;
    public int blockSize;
    public int totalBlocks;
    public int maxInodes;

    //Estados de los bloques
    public int freeBlocks;
    public int usedBlocks;
    public int freeInodes;
    public int usedInodes;

    //Los offsets de los .fs
    public long bitmapOffset;
    public long inodeTableOffset;
    public long dataBlocksOffset;
    public long userTableOffset;
    public long groupTableOffset;

    public SuperBlock() {
    }

    public void init(String name, long diskSizeBytes, int blockSize, String fileName) {
        
        this.magic = MAGIC;
        this.filename = filename;
        
       
        this.fsName = name;
        this.createdAt = System.currentTimeMillis();
        this.lastMounted = System.currentTimeMillis();

        this.totalSizeBytes = diskSizeBytes;
        this.blockSize = blockSize;
        this.totalBlocks = (int) (diskSizeBytes / blockSize);
        this.maxInodes = totalBlocks / 10; //un 10% que seria un inodo por cada 10 bloques

        this.freeBlocks  = totalBlocks;
        this.usedBlocks  = 0;
        this.freeInodes  = maxInodes;
        this.usedInodes  = 0;

        // Se calculan los offsets -> puro calculo mate
        // MBR ocupa el primer bloque
        long mbrEnd = blockSize;

        // Superbloque ocupa el segundo bloque
        long sbEnd = mbrEnd + blockSize;

        // el Bitmap va despu del superbloque
        this.bitmapOffset = sbEnd;
        int bitmapSize = (totalBlocks / 8) + 1;

        // la tabla de inodos va despues del bitmap
        this.inodeTableOffset = bitmapOffset + bitmapSize;

        // Bloques de datos van despues de la tabla de inodos
        int inodeSize = 256; // -> 
        this.dataBlocksOffset = inodeTableOffset + ((long) maxInodes * inodeSize);
        this.userTableOffset = dataBlocksOffset + ((long) totalBlocks * blockSize);
        this.groupTableOffset = userTableOffset + ((long) User.MAX_USERS * User.USER_SIZE);
    }

    public boolean isValid() {
        return this.magic == MAGIC;
    }
    
    // Se llama cada vez que se asignan bloques (touch, mkdir, etc)
    public void blockUsed(int count) {
        usedBlocks += count;
        freeBlocks -= count;
    }

    // Se llama cada vez que se liberan bloques (rm, etc)
    public void blockFreed(int count) {
        usedBlocks -= count;
        freeBlocks += count;
    }

    public void inodeUsed() {
        usedInodes++;
        freeInodes--;
    }

    public void inodeFreed() {
        usedInodes--;
        freeInodes++;
    }

    public void updateLastMounted() {
        this.lastMounted = System.currentTimeMillis();
    }

    // info FS
    public void print() {
        System.out.println("----- infoFS -----");
        System.out.println("Nombre:            " + fsName);
        System.out.println("Tamaño total:      " + totalSizeBytes / (1024 * 1024) + " MB (" + totalSizeBytes + " bytes)");
        System.out.println("Tamaño de bloque:  " + blockSize + " bytes");
        System.out.println("Bloques totales:   " + totalBlocks);
        System.out.println("Bloques usados:    " + usedBlocks);
        System.out.println("Bloques libres:    " + freeBlocks);
        System.out.println("Inodos totales:    " + maxInodes);
        System.out.println("Inodos usados:     " + usedInodes);
        System.out.println("Inodos libres:     " + freeInodes);
        System.out.println("Creado:            " + new java.util.Date(createdAt));
        System.out.println("Último montaje:    " + new java.util.Date(lastMounted));
        System.out.println("----------------------");
    }
}


