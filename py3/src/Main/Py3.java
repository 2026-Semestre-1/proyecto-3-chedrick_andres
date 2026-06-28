package Main;

import fileSystem.FileSystem;
import shell.Shell;
import java.io.File;
import java.util.Scanner;

public class Py3 {

    public static void main(String[] args) throws ClassNotFoundException {
        Scanner sc = new Scanner(System.in);

        System.out.println("Inserte un comando de ejecución:");
        String lineaCompleta = sc.nextLine().trim();

        String[] partes = lineaCompleta.split("\\s+");

        if (partes.length < 2 || !partes[0].equals("java") || !partes[1].equals("myFileSystem")) {
            System.out.println("Error: comando inválido. Use: java myFileSystem [nombre_disco.fs]");
            return;
        }

        String filename = null;
        if (partes.length >= 3) {
            filename = partes[2];
        }

        FileSystem fs = new FileSystem();

        if (filename == null) {
            //Creamis disco nuevo
            System.out.println("Inserte el comando format para empezar.");
            System.out.println("Ejemplo: format 10 512 miDiscoDuro.fs");
            System.out.println("(tamaño en MB, tamaño de bloque en bytes, nombre del archivo)");

            boolean formatoValido = false;

            while (!formatoValido) {
                System.out.print("ccampos@miFS: ");
                String comandoFormat = sc.nextLine().trim();
                String[] partesFormat = comandoFormat.split("\\s+");

                if (partesFormat.length < 4 || !partesFormat[0].equals("format")) {
                    System.out.println("Error: formato incorrecto.");
                    System.out.println("Use exactamente: format <tamaño_MB> <tamaño_bloque> <nombre_archivo.fs>");
                    System.out.println("Ejemplo: format 10 512 miDiscoDuro.fs");
                    continue;
                }

                int diskMB;
                int blockSize;
                try {
                    diskMB = Integer.parseInt(partesFormat[1]);
                    blockSize = Integer.parseInt(partesFormat[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Error: el tamaño y el tamaño de bloque deben ser números.");
                    System.out.println("Ejemplo: format 10 512 miDiscoDuro.fs");
                    continue;
                }

                if (diskMB <= 0 || blockSize <= 0) {
                    System.out.println("Error: el tamaño del disco y del bloque deben ser mayores a 0.");
                    continue;
                }

                String nombreDisco = partesFormat[3];

                fs.format(diskMB, blockSize, nombreDisco, sc);
                formatoValido = true;
            }

        } else {
            File archivoDisco = new File(filename);

            if (!archivoDisco.exists()) {
                System.out.println("Error: el archivo '" + filename + "' no existe");
                return;
            }

            System.out.println("Cargando disco existente: " + filename);
            boolean cargado = fs.load(filename);

            if (!cargado) {
                System.out.println("No se pudo cargar el disco. Cerrando programa.");
                return;
            }
        }

        Shell shell = new Shell(fs);
        shell.run();
    }
}