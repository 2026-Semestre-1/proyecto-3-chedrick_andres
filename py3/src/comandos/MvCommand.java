/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;
import java.io.IOException;

/**
 *
 * @author joses
 */
public class MvCommand implements Command {

    private FileSystem fs;
    private ShellState state;

    public MvCommand(FileSystem fs, ShellState state) {
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 3) {
            System.out.println("Uso: mv nombre destino_o_nuevo_nombre");
            return;
        }

        String name = args[1];
        String segundoArg = args[2];
        int dirActual = state.currentDirId;

        int sourceId = fs.buscarInodePorNombre(name, dirActual);
        if (sourceId == -1) {
            System.out.println("Error: '" + name + "' no existe");
            return;
        }

        // si el segundo argumento es un directorio existente - -  mover
        int destDirId = fs.buscarInodePorNombre(segundoArg, dirActual);
        boolean esDirectorioDestino = destDirId != -1
                && fs.inodeTable[destDirId].type.equals(Inode.DIR);

        if (esDirectorioDestino) {
            mover(sourceId, dirActual, destDirId);
        } else {
            renombrar(sourceId, dirActual, segundoArg);
        }
    }

    // Mover a otro directorio 
    private void mover(int sourceId, int currentParentId, int newParentId) {
        try {
            Inode source = fs.inodeTable[sourceId];
            Inode oldParent = fs.inodeTable[currentParentId];
            Inode newParent = fs.inodeTable[newParentId];

            //  no repetidos
            for (int i = 0; i < newParent.childCount; i++) {
                Inode child = fs.inodeTable[newParent.children[i]];
                if (child != null && child.getFullName().equals(source.getFullName())) {
                    System.out.println("Error: ya existe '" + source.getFullName() + "' en el destino");
                    return;
                }
            }

            // quita al padre viejo, agregar al padre nuevo
            oldParent.removeChild(sourceId);
            newParent.addChild(sourceId);
            source.parentId = newParentId;

            // Guardar los tres inodos afectados
            fs.diskManager.writeInode(source, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeInode(oldParent, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeInode(newParent, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);

            System.out.println("'" + source.getFullName() + "' movido correctamente");

        } catch (IOException e) {
            System.out.println("Error al mover: " + e.getMessage());
        }
    }

    
    private void renombrar(int sourceId, int parentId, String nuevoNombre) {
        try {
            Inode source = fs.inodeTable[sourceId];
            Inode parent = fs.inodeTable[parentId];

            
            for (int i = 0; i < parent.childCount; i++) {
                Inode child = fs.inodeTable[parent.children[i]];
                if (child != null && child.id != sourceId && child.getFullName().equals(nuevoNombre)) {
                    System.out.println("Error: ya existe '" + nuevoNombre + "' en este directorio");
                    return;
                }
            }

            String nombreViejo = source.getFullName();

        
            if (source.type.equals(Inode.FILE) && nuevoNombre.contains(".")) {
                int dot = nuevoNombre.lastIndexOf(".");
                source.name = nuevoNombre.substring(0, dot);
                source.extension = nuevoNombre.substring(dot + 1);
            } else {
                source.name = nuevoNombre;
                source.extension = "";
            }

            fs.diskManager.writeInode(source, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);

            System.out.println("'" + nombreViejo + "' renombrado a '" + nuevoNombre + "'");

        } catch (IOException e) {
            System.out.println("Error al renombrar: " + e.getMessage());
        }
    }
}