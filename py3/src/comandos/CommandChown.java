/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comandos;

import fileSystem.FileSystem;
import java.io.IOException;
import nucleo.Inode;
import nucleo.User;

/**
 *
 * @author chedr
 */
public class CommandChown implements Command {
    public FileSystem fs;
    public ShellState state;

    public CommandChown(FileSystem fs, ShellState state) {
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 3 || args.length > 4) {
            System.out.println("Error uso: chown username filename / chown username directory / chown -R username directory");
            return;
        }

        Inode inodeActualizado = null;
        boolean asignado = false;
        int newOwner;
        boolean inodeValido;

        switch (args.length) {
            case 3:
                newOwner = -1;
                for (User user : fs.userTable) {
                    if (user != null && user.username != null && user.username.equals(args[1])) {
                        newOwner = user.userId;
                        break;
                    }
                }
                if (newOwner == -1) {
                    System.out.println("Error: el usuario '" + args[1] + "' no existe");
                    return;
                }

                String nombreFinal3 = extraerNombre(args[2]);
                boolean esArchivo3  = esArchivo(nombreFinal3);

                inodeValido = false;
                for (Inode inode : fs.inodeTable) {
                    if (inode != null && inode.name != null && inode.getFullName().equals(nombreFinal3)) {

                        if (esArchivo3 && !inode.type.equals(Inode.FILE)) continue;
                        if (!esArchivo3 && !inode.type.equals(Inode.DIR)) continue;

                        inodeActualizado = inode;
                        if (state.currentUserId == 0 || state.currentUserId == inode.ownerId) {
                            inodeValido = true;
                            inode.ownerId = newOwner;
                            asignado = true;
                            break;
                        }
                    }
                }

                if (!inodeValido && inodeActualizado == null) {
                    System.out.println("Error: '" + nombreFinal3 + "' no existe");
                    return;
                }

                if (!inodeValido) {
                    System.out.println("Error: solo root o el dueño puede cambiar el propietario");
                    return;
                }

                if (asignado) {
                    try {
                        fs.diskManager.writeInode(inodeActualizado, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
                        System.out.println("Éxito: El usuario '" + args[1] + "' es el nuevo dueño de '" + nombreFinal3 + "'");
                    } catch (IOException ex) {
                        System.out.println("Error al escribir en disco: " + ex.getMessage());
                    }
                    return;
                }
                break;

            case 4:

                if (!args[1].equals("-R")) {
                    System.out.println("Error uso: chown -R username directory");
                    return;
                }

                newOwner = -1;
                for (User user : fs.userTable) {
                    if (user != null && user.username != null && user.username.equals(args[2])) {
                        newOwner = user.userId;
                        break;
                    }
                }
                if (newOwner == -1) {
                    System.out.println("Error: el usuario '" + args[2] + "' no existe");
                    return;
                }

                String nombreFinal4 = extraerNombre(args[3]);


                if (esArchivo(nombreFinal4)) {
                    System.out.println("Error: '-R' se usa con directorios, no con archivos");
                    return;
                }

                inodeValido = false;
                for (Inode inode : fs.inodeTable) {
                    if (inode != null && inode.name != null
                            && inode.getFullName().equals(nombreFinal4)
                            && inode.type.equals(Inode.DIR)) {

                        inodeActualizado = inode;
                        if (state.currentUserId == 0 || state.currentUserId == inode.ownerId) {
                            inodeValido = true;
                            asignado = true;
                            break;
                        }
                    }
                }

                if (inodeActualizado == null) {
                    System.out.println("Error: directorio '" + nombreFinal4 + "' no existe");
                    return;
                }

                if (!inodeValido) {
                    System.out.println("Error: solo root o el dueño puede cambiar el propietario");
                    return;
                }

                cambiarRecursivo(inodeActualizado, newOwner);
                System.out.println("Éxito: '" + args[2] + "' es el nuevo dueño de '" + nombreFinal4 + "' y su contenido");
                break;
        }
    }


    private String extraerNombre(String path) {
        if (path.contains("/")) {
            return path.substring(path.lastIndexOf("/") + 1);
        }
        return path;
    }

    private boolean esArchivo(String nombre) {
        return nombre.contains(".");
    }

    private void cambiarRecursivo(Inode inode, int newOwner) {
        inode.ownerId = newOwner;
        try {
            fs.diskManager.writeInode(inode, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
        } catch (IOException ex) {
            System.out.println("Error al escribir en disco: " + ex.getMessage());
            return;
        }
        if (inode.type.equals(Inode.DIR)) {
            for (int i = 0; i < inode.childCount; i++) {
                Inode hijo = fs.inodeTable[inode.children[i]];
                if (hijo != null) {
                    cambiarRecursivo(hijo, newOwner);
                }
            }
        }
    }
}