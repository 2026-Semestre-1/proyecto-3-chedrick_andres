/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package administradorDisco;
import java.io.*;
import nucleo.Group;
import nucleo.Inode;
import nucleo.MapaDeBits;
import nucleo.SuperBlock;
import nucleo.User;
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
    
    
    public Inode[] readAllInodes(long inodeTableOffset, int inodeSize, int maxInodes) throws IOException {
    Inode[] tabla = new Inode[maxInodes];

    for (int i = 0; i < maxInodes; i++) {
        try {
            long pos = inodeTableOffset + ((long) i * inodeSize);
            disk.seek(pos);
            byte[] data = new byte[inodeSize];
            disk.read(data);

            // DEBUG temporal
            if (i == 0) {
                System.out.println("DEBUG pos=" + pos);
                System.out.print("DEBUG primeros 10 bytes: ");
                for (int j = 0; j < 10; j++) {
                    System.out.print(data[j] + " ");
                }
                System.out.println();
                System.out.println("DEBUG esVacio? " + esVacio(data));
            }

            if (esVacio(data)) {
                tabla[i] = null;
            } else {
                tabla[i] = (Inode) deserialize(data);
            }
        } catch (Exception e) {
            if (i == 0) {
                System.out.println("DEBUG excepción en i=0: " + e.getClass().getName() + " - " + e.getMessage());
            }
            tabla[i] = null;
        }
    }

    return tabla;
}

    private boolean esVacio(byte[] data) {
        for (int i = 0; i < Math.min(20, data.length); i++) {
            if (data[i] != 0) return false;
        }
        return true;
    }

    // Crea el archivo con el size
    public void createDisk(long sizeBytes) throws IOException {
        File f = new File(filename);
        if (f.exists()) f.delete();
        disk = new RandomAccessFile(filename, "rw");
        disk.setLength(sizeBytes);
        disk.close();
        System.out.println("Disco creado: " + filename + " (" + sizeBytes + " bytes)");
    }

    public void openDisk() throws IOException {
        disk = new RandomAccessFile(filename, "rw");
    }

    public void closeDisk() throws IOException {
        if (disk != null) disk.close();
    }

    // ===== SuperBlock =====
    public void writeSuperBlock(SuperBlock sb) throws IOException {
    byte[] data = serialize(sb);
    System.out.println("DEBUG writeSuperBlock: data.length=" + data.length);
    disk.seek(0);
    disk.write(data);
}

    public SuperBlock readSuperBlock(int maxBytes) throws IOException, ClassNotFoundException {
        disk.seek(0);
        byte[] data = new byte[maxBytes];
        disk.read(data);
        return (SuperBlock) deserialize(data);
    }

    // ===== Bitmap-Bug arreglado con chat =====
    // Se guarda en la posición que indica sb.bitmapOffset
    public void writeBitmap(MapaDeBits bitmap, long offset) throws IOException {
    byte[] data = serialize(bitmap);

    // Calculamos el espacio máximo disponible para el bitmap
    // (la diferencia entre donde empieza la tabla de inodos y donde empieza el bitmap)
    // Por seguridad, usamos un tamaño fijo con margen, igual que con los inodos

    int bitmapMaxSize = 3000; // margen seguro para tu bitmapSize real (~2561) + overhead de serialización

    if (data.length > bitmapMaxSize) {
        throw new IOException("El bitmap serializado pesa más de lo reservado (" + data.length + " > " + bitmapMaxSize + ")");
    }

    byte[] bloqueCompleto = new byte[bitmapMaxSize];
    System.arraycopy(data, 0, bloqueCompleto, 0, data.length);

    disk.seek(offset);
    disk.write(bloqueCompleto);
}

    public MapaDeBits readBitmap(long offset, int maxBytes) throws IOException, ClassNotFoundException {
        disk.seek(offset);
        byte[] data = new byte[maxBytes];
        disk.read(data);
        return (MapaDeBits) deserialize(data);
    }

    // ===== Tabla de inodos =====
    // Cada inodo ocupa "inodeSize" bytes fijos, así podemos calcular su posición exacta
    public void writeInode(Inode inode, long inodeTableOffset, int inodeSize) throws IOException {
    byte[] data = serialize(inode);
    if (data.length > inodeSize) {
        throw new IOException("El inodo serializado pesa más de lo reservado (" + data.length + " > " + inodeSize + ")");
    }

    byte[] bloqueCompleto = new byte[inodeSize];
    System.arraycopy(data, 0, bloqueCompleto, 0, data.length);

    long pos = inodeTableOffset + ((long) inode.id * inodeSize);

    // DEBUG temporal
    if (inode.id == 0) {
        System.out.println("DEBUG escribiendo inodo id=0 en pos=" + pos + ", data.length=" + data.length);
    }

    disk.seek(pos);
    disk.write(bloqueCompleto);

    // DEBUG: confirmar que se escribió
    if (inode.id == 0) {
        disk.seek(pos);
        byte[] verificacion = new byte[10];
        disk.read(verificacion);
        System.out.print("DEBUG verificación inmediata tras escribir: ");
        for (byte b : verificacion) System.out.print(b + " ");
        System.out.println();
    }
}

    public Inode readInode(int inodeId, long inodeTableOffset, int inodeSize) throws IOException, ClassNotFoundException {
        long pos = inodeTableOffset + ((long) inodeId * inodeSize);
        disk.seek(pos);
        byte[] data = new byte[inodeSize];
        disk.read(data);
        return (Inode) deserialize(data);
    }

    //  Bloques de datos reales
    // Esto es lo que usan touch, cat, note para escribir/leer contenido
    public void writeBlock(int blockNum, byte[] content, long dataBlocksOffset, int blockSize) throws IOException {
        if (content.length > blockSize) {
            throw new IOException("El contenido excede el tamaño de un bloque");
        }
        long pos = dataBlocksOffset + ((long) blockNum * blockSize);
        disk.seek(pos);
        disk.write(content);
    }

    public byte[] readBlock(int blockNum, long dataBlocksOffset, int blockSize) throws IOException {
        long pos = dataBlocksOffset + ((long) blockNum * blockSize);
        disk.seek(pos);
        byte[] data = new byte[blockSize];
        disk.read(data);
        return data;
    }

 
    private byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.flush();
        return baos.toByteArray();
    }

    private Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(bais);
        return ois.readObject();
    }

    public long getFileSize() {
        return new File(filename).length();
    }
    public void writeUser(User user, long userTableOffset, int userSize) throws IOException {
        byte[] data = serialize(user);
        long pos = userTableOffset + ((long) user.userId * userSize);
        disk.seek(pos);
        disk.write(data);
    }

    public User readUser(int userId, long userTableOffset, int userSize) 
        throws IOException, ClassNotFoundException {
        long pos = userTableOffset + ((long) userId * userSize);
        disk.seek(pos);
        byte[] data = new byte[userSize];
        disk.read(data);
        return (User) deserialize(data);
    }
    public void writeGroup(Group group, long groupTableOffset, int groupSize) throws IOException {
        byte[] data = serialize(group);
        long pos = groupTableOffset + ((long) group.groupId * groupSize);
        disk.seek(pos);
        disk.write(data);
    }
 
    public Group readGroup(int groupId, long groupTableOffset, int groupSize)
        throws IOException, ClassNotFoundException {
        long pos = groupTableOffset + ((long) groupId * groupSize);
        disk.seek(pos);
        byte[] data = new byte[groupSize];
        disk.read(data);
        return (Group) deserialize(data);
    }
}