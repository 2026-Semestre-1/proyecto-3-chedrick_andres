/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package administradorDisco;
import java.io.*;
import nucleo.SuperBlock;

/**
 *
 * @author joses
 */
public class DiskManager {

    private String filename;
    private RandomAccessFile disk;

    public DiskManager(String filename) {
        this.filename = filename;
    }

    // Crea el archivo con el size
    public void createDisk(long sizeBytes) throws IOException {
        File f = new File(filename);
        if (f.exists()) f.delete(); // si ya existe, lo recreamos

        disk = new RandomAccessFile(filename, "rw");
        disk.setLength(sizeBytes); // esto crea el archivo con ese peso exacto
        disk.close();

        System.out.println("Disco creado: " + filename + " (" + sizeBytes + " bytes)");
    }

    // Abre un disco ya existente
    public void openDisk() throws IOException {
        disk = new RandomAccessFile(filename, "rw");
    }

    public void closeDisk() throws IOException {
        if (disk != null) disk.close();
    }

    // Guarda el SuperBlock al inicio del archivo y serializado
    public void writeSuperBlock(SuperBlock sb) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(sb);
        oos.flush();
        byte[] data = baos.toByteArray();

        disk.seek(0); // el superblock va al inicio
        disk.write(data);
    }

    // Lee el SuperBlock desde el archivo
    public SuperBlock readSuperBlock(int maxBytes) throws IOException, ClassNotFoundException {
        disk.seek(0);
        byte[] data = new byte[maxBytes];
        disk.read(data);

        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return (SuperBlock) ois.readObject();
    }

    // Para comprobar visualmente que el archivo existe y pesa lo correcto
    public long getFileSize() {
        return new File(filename).length();
    }
}
