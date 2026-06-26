/*
* Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
*/
package comandos;

import fileSystem.FileSystem;
import java.io.IOException;
import java.util.Scanner;
import nucleo.Inode;

/**
 *
 * @author joses
 */
public class NoteCommand implements Command {

    private FileSystem fs;
    private ShellState state;
    private Scanner scanner;

    public NoteCommand(FileSystem fs, ShellState state, Scanner scanner) {
        this.fs = fs;
        this.state = state;
        this.scanner = scanner;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: note nombre");
            return;
        }

        String nombre = args[1];
        int inodeId = fs.buscarInodePorNombre(nombre, state.currentDirId);

        Inode inode;

        // Si el archivo no existe, lo creamos
        if (inodeId == -1) {
            System.out.println("'" + nombre + "' no existe, se creará al guardar");
            inode = null;
        } else {
            inode = fs.inodeTable[inodeId];

            if (!inode.type.equals(Inode.FILE)) {
                System.out.println("Error: '" + nombre + "' no es un archivo");
                return;
            }

            // Validacin de permisos: solo se abre si tiene acceso
            int ownerId = state.currentUserId;
            int groupId = -1;
            for(int i = 0; i < fs.userTable.length; i++) {
                if (fs.userTable[i] != null && fs.userTable[i].userId == ownerId) {
                    groupId = fs.userTable[i].groupId;
                    break;
                }
            }

            if (!inode.canWrite(ownerId, groupId)) {
                System.out.println("Error: no tiene permisos para editar '" + nombre + "'");
                return;
            }
        }

        StringBuilder contenidoActual = new StringBuilder();
        if (inode != null) {
            contenidoActual.append(leerContenido(inode));
        }

        System.out.println("===== Editor note: " + nombre + " =====");
        System.out.println("Contenido actual:");
        System.out.println(contenidoActual.length() > 0 ? contenidoActual.toString() : "(vacío)");
        System.out.println();
        System.out.println("Escriba el nuevo contenido. Para salir, escriba: :wq");
        System.out.println("(esto simula Ctrl+X en este entorno de consola)");

        StringBuilder nuevoContenido = new StringBuilder();
        boolean editando = true;

        while (editando) {
            String linea = scanner.nextLine();

            if (linea.equals(":wq")) {
                editando = false;
            } else {
                nuevoContenido.append(linea).append("\n");
            }
        }

        // Preguntar si desea guardar
        System.out.print("¿Desea guardar los cambios? (s/n): ");
        String respuesta = scanner.nextLine().trim().toLowerCase();

        if (respuesta.equals("s")) {
            guardar(nombre, inodeId, nuevoContenido.toString());
        } else {
            System.out.println("Cambios descartados");
        }
    }

    private String leerContenido(Inode inode) {
        try {
            int[] blocks = inode.getUsedBlocks();
            if (blocks.length == 0)
                return "";

            StringBuilder contenido = new StringBuilder();
            int blockSize = fs.superblock.blockSize;
            int bytesLeidos = 0;

            for (int blockNum : blocks) {
                byte[] data = fs.diskManager.readBlock(blockNum, fs.superblock.dataBlocksOffset, blockSize);
                int bytesEnEsteBloque = Math.min(blockSize, inode.sizeBytes - bytesLeidos);
                contenido.append(new String(data, 0, bytesEnEsteBloque));
                bytesLeidos += bytesEnEsteBloque;
            }
            return contenido.toString();
        } catch (IOException e) {
            return "(error al leer contenido)";
        }
    }

    private void guardar(String nombre, int inodeId, String contenido) {
        try {
            // Si el archivo no existía, lo creamos primero (reusa la lógica de touch)
            if (inodeId == -1) {
                crearArchivo(nombre);
                inodeId = fs.buscarInodePorNombre(nombre, state.currentDirId);
                if (inodeId == -1) {
                    System.out.println("Error: no se pudo crear el archivo");
                    return;
                }
            }

            Inode inode = fs.inodeTable[inodeId];
            byte[] bytes = contenido.getBytes();
            int blockSize = fs.superblock.blockSize;
            int neededBlocks = (int) Math.ceil((double) bytes.length / blockSize);
            if (neededBlocks == 0)
                neededBlocks = 1;

            if (neededBlocks > Inode.MAX_BLOCKS) {
                System.out.println("Error: el contenido es muy grande para este archivo");
                return;
            }

            fs.bitmap.free(inode.getUsedBlocks());
            int[] nuevosBloques = fs.bitmap.allocate(neededBlocks);

            if (nuevosBloques == null) {
                System.out.println("Error: no hay espacio en disco");
                return;
            }

            for (int i = 0; i < Inode.MAX_BLOCKS; i++)
                inode.blockIndex[i] = -1;
            inode.setBlocks(nuevosBloques);
            inode.sizeBytes = bytes.length;

            int offset = 0;
            for (int blockNum : nuevosBloques) {
                int len = Math.min(blockSize, bytes.length - offset);
                byte[] chunk = new byte[blockSize];
                System.arraycopy(bytes, offset, chunk, 0, len);
                fs.diskManager.writeBlock(blockNum, chunk, fs.superblock.dataBlocksOffset, blockSize);
                offset += len;
            }

            fs.diskManager.writeInode(inode, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeBitmap(fs.bitmap, fs.superblock.bitmapOffset);
            fs.diskManager.writeSuperBlock(fs.superblock);

            System.out.println("Archivo guardado correctamente");

        } catch (IOException e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }

    // Crea el archivo vacío si note se usó sobre un nombre que no existía
    private void crearArchivo(String nombre) {
        try {
            int parentId = state.currentDirId;
            Inode parent = fs.inodeTable[parentId];

            int newId = -1;
            for (int i = 0; i < fs.inodeTable.length; i++) {
                if (fs.inodeTable[i] == null) {
                    newId = i;
                    break;
                }
            }
            if (newId == -1) {
                System.out.println("Error: tabla de inodos llena");
                return;
            }
            int ownerId = state.currentUserId;
            int groupId = -1;
            for(int i = 0; i < fs.userTable.length; i++) {
                if (fs.userTable[i] != null && fs.userTable[i].userId == ownerId) {
                    groupId = fs.userTable[i].groupId;
                    break;
                }
            }

            Inode nuevo = new Inode();
            nuevo.init(newId, nombre, Inode.FILE, state.currentUserId, groupId, parentId);
            nuevo.ownerPerm = 6;
            nuevo.groupPerm = 4;
            nuevo.sizeBytes = 0;

            parent.addChild(newId);
            fs.inodeTable[newId] = nuevo;
            fs.superblock.inodeUsed();

            fs.diskManager.writeInode(nuevo, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeInode(parent, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeSuperBlock(fs.superblock);

        } catch (IOException e) {
            System.out.println("Error al crear archivo: " + e.getMessage());
        }
    }
}