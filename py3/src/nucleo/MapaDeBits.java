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
public class MapaDeBits implements Serializable{
    private byte[] bits;
    private int totalBlocks;
    
    public MapaDeBits(int totalBlocks){
        this.totalBlocks = totalBlocks;
        this.bits = new byte[(totalBlocks / 8) + 1];
    }
    
    //Marcamos el blocque como marcado
    public void set(int block){
        bits[block / 8] |= (1 << (7 - (block % 8)));
    }
    
    // Marca un bloque como libre
    public void clear(int block) {
        bits[block / 8] &= ~(1 << (7 - (block % 8)));
    }

    // Revisa si un bloque está libre
    public boolean isFree(int block) {
        return (bits[block / 8] & (1 << (7 - (block % 8)))) == 0;
    }

    // Busca N bloques libres, los marca y los devuelve
    public int[] allocate(int count) {
        int[] result = new int[count];
        int found = 0;

        for (int i = 0; i < totalBlocks && found < count; i++) {
            if (isFree(i)) {
                result[found] = i;
                found++;
            }
        }

        if (found < count) {
            System.out.println("Error: no hay suficiente espacio en el disco");
            return null;
        }

        for (int i = 0; i < count; i++) {
            set(result[i]);
        }

        return result;
    }

    // Libera los bloques de un archivo
    public void free(int[] blocks) {
        for (int b : blocks) {
            if (b != -1) clear(b);
        }
    }

    //  bloques que necesita un archivo segun su tamaño
    public int blocksNeeded(int fileSizeBytes, int blockSize) {
        return (int) Math.ceil((double) fileSizeBytes / blockSize);
    }

    //  devuelve el estado de cada bloque
    public boolean[] getMap() {
        boolean[] map = new boolean[totalBlocks];
        for (int i = 0; i < totalBlocks; i++) {
            map[i] = !isFree(i); // true = ocupado
        }
        return map;
    }

    // Muestra los primeros N bloques en consola 
    public void print(int howMany) {
        System.out.print("Bitmap [");
        for (int i = 0; i < howMany && i < totalBlocks; i++) {
            System.out.print(isFree(i) ? "0" : "1");
            if (i < howMany - 1) System.out.print(", ");
        }
        System.out.println("]");
    }

    // Muestra el bitmap en hexadecimal 
    public void printHex() {
        System.out.print("Bitmap hex: ");
        for (int i = 0; i < Math.min(bits.length, 16); i++) {
            System.out.printf("%02X ", bits[i]);
        }
        System.out.println();
    }
    
}
