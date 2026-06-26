/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package Main;

import fileSystem.FileSystem;
import shell.Shell;

/**
 *
 * @author joses
 */
public class Py3 {

    public static void main(String[] args) throws ClassNotFoundException {
        FileSystem fs = new FileSystem();

        if (args.length == 0) {
            System.out.println("No se especificó disco. Creando uno nuevo...");
            fs.format(10, 512, "miDiscoDuro.fs");
        } else {
            String filename = args[0];
            boolean cargado = fs.load(filename);

            if (!cargado) {
                System.out.println("No se pudo cargar el disco. Cerrando programa.");
                return;
            }
        }

        // En Py3.java, justo después de fs.load(filename) exitoso, ANTES de crear el Shell
        if (fs.inodeTable[0] == null) {
            System.out.println("DEBUG: inodeTable[0] es null");
        } else {
            System.out.println("DEBUG: inodeTable[0].type = " + fs.inodeTable[0].type);
            System.out.println("DEBUG: inodeTable[0].name = " + fs.inodeTable[0].name);
        }
        
        

        Shell shell = new Shell(fs);
        shell.run();
    }
}
