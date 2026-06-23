/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gestorArchivos;


import nucleo.SuperBlock;
import administradorDisco.DiskManager;

/**
 *
 * @author joses
 */
public class GestorArchivos {

    public static void main(String[] args) {

        try {
            String filename = "miDiscoDuro.fs";
            int diskMB = 10;       // el usuario pide 10MB
            int blockSize = 512;   // el usuario pide bloques de 512

            long diskSizeBytes = diskMB * 1024L * 1024L;

            System.out.println("===== Ejecutando format =====");
            System.out.println("Tamaño solicitado: " + diskMB + " MB");
            System.out.println("Tamaño de bloque:  " + blockSize + " bytes");

            // 1. Crear el SuperBlock en memoria
            SuperBlock sb = new SuperBlock();
            sb.init("miFS", diskSizeBytes, blockSize, filename);

            // 2. Crear el archivo físico en disco
            DiskManager dm = new DiskManager(filename);
            dm.createDisk(diskSizeBytes);

            // 3. Abrirlo y escribir el SuperBlock dentro
            dm.openDisk();
            dm.writeSuperBlock(sb);

            System.out.println();
            System.out.println("===== Verificación =====");
            System.out.println("Tamaño real del archivo en disco: " + dm.getFileSize() + " bytes");
            System.out.println("Coincide con lo solicitado? " + (dm.getFileSize() == diskSizeBytes));

            // 4. Leer el SuperBlock de vuelta, como si abrieras el disco después
            SuperBlock sbLeido = dm.readSuperBlock(2048); // margen de bytes
            System.out.println();
            System.out.println("===== SuperBlock leído desde el archivo =====");
            sbLeido.print();
            System.out.println("Magic válido? " + sbLeido.isValid());

            dm.closeDisk();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}