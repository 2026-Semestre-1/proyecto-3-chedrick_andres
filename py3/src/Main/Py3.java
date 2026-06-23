/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package Main;

import fileSystem.FileSystem;
import shell.Shell;

/**
 *
 * @author joses
 */
public class Py3 {

    public static void main(String[] args) {
        FileSystem fs = new FileSystem();

        if (args.length == 0) {
            // java myFileSystem -> formatea uno nuevo
            System.out.println("No se especificó disco. Creando uno nuevo...");
            System.out.print("Tamaño del disco en MB: ");
            // Por ahora lo dejamos fijo para probar; luego usamos Scanner aquí también
            fs.format(10, 512, "miDiscoDuro.fs");
        } else {
            // java myFileSystem miDiscoDuro.fs -> cargar uno existente
            System.out.println("(pendiente: cargar disco existente: " + args[0] + ")");
            return;
        }

        Shell shell = new Shell(fs);
        shell.run();
    }
}