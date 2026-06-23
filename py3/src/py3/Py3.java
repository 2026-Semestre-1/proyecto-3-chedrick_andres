/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package py3;

import fileSystem.FileSystem;

/**
 *
 * @author joses
 */
public class Py3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        FileSystem fs = new FileSystem();
        fs.format(10, 512, "miDiscoDuro.fs");

        fs.mkdir("documents", 0, 0, 0);
        fs.mkdir("images", 0, 0, 0);
        fs.touch("notas.txt", 0, 0, 0);

        // documents debería tener id=2 (root=0, /root=1, documents=2)
        fs.touch("reporte.txt", 2, 0, 0);

        System.out.println();
        System.out.println("===== ls (simple) =====");
        fs.ls(0, false);

        System.out.println();
        System.out.println("===== ls -R (recursivo) =====");
        fs.ls(0, true);
    }

}