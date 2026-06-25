/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;
import java.io.IOException;

/**
 *
 * @author joses
 */
public class HexCheckCommand implements Command {

    private FileSystem fs;
    private ShellState state;

    public HexCheckCommand(FileSystem fs, ShellState state) {
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: hexdump nombre_archivo");
            return;
        }

        String nombre = args[1];
        int inodeId = fs.buscarInodePorNombre(nombre, state.currentDirId);

        if (inodeId == -1) {
            System.out.println("Error: '" + nombre + "' no existe");
            return;
        }

        Inode inode = fs.inodeTable[inodeId];

        if (!inode.type.equals(Inode.FILE)) {
            System.out.println("Error: '" + nombre + "' no es un archivo");
            return;
        }

        int[] blocks = inode.getUsedBlocks();

        System.out.println("Distribución de bloques: " + nombre + " =====");
        System.out.println("Tamaño del archivo: " + inode.sizeBytes + " bytes");
        System.out.println("Tamaño de bloque:   " + fs.superblock.blockSize + " bytes");
        System.out.println("Bloques usados:     " + blocks.length + " -> " + arrayToString(blocks));
        System.out.println();

        try {
            for (int i = 0; i < blocks.length; i++) {
                int blockNum = blocks[i];
                long posicionReal = fs.superblock.dataBlocksOffset + ((long) blockNum * fs.superblock.blockSize);

                byte[] data = fs.diskManager.readBlock(blockNum, fs.superblock.dataBlocksOffset, fs.superblock.blockSize);

                System.out.println("--- Bloque #" + blockNum + " (posición real en .fs: byte " + posicionReal + ") ---");
                imprimirHex(data, Math.min(64, data.length)); // primeros 64 bytes del bloque
                System.out.println();
            }
        } catch (IOException e) {
            System.out.println("Error al leer bloques: " + e.getMessage());
        }
    }

    private void imprimirHex(byte[] data, int cantidad) {
        StringBuilder hex = new StringBuilder();
        StringBuilder ascii = new StringBuilder();

        for (int i = 0; i < cantidad; i++) {
            hex.append(String.format("%02X ", data[i]));
            char c = (char) data[i];
            ascii.append((c >= 32 && c < 127) ? c : '.');

            if ((i + 1) % 16 == 0) {
                System.out.println(hex.toString() + "  | " + ascii.toString());
                hex.setLength(0);
                ascii.setLength(0);
            }
        }
        if (hex.length() > 0) {
            System.out.println(hex.toString() + "  | " + ascii.toString());
        }
    }

    private String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            sb.append(arr[i]);
            if (i < arr.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}

