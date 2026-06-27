/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comandos;

import fileSystem.FileSystem;
import java.io.IOException;
import nucleo.Group;
import nucleo.Inode;

public class CommandChgrp implements Command {

    public FileSystem fs;
    public ShellState state;

    public CommandChgrp(FileSystem fs, ShellState state) {
        this.fs    = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 3 || args.length > 4) {
            System.out.println("Error uso: chgrp groupname filename / chgrp groupname directory / chgrp -R groupname directory");
            return;
        }

        Inode inodeActualizado = null;
        boolean asignado      = false;
        int newGroup;
        boolean inodeValido;

        switch (args.length) {
            case 3:

                newGroup = -1;
                for (Group group : fs.groupTable) {
                    if (group != null && group.groupName != null && group.groupName.equals(args[1])) {
                        newGroup = group.groupId;
                        break;
                    }
                }
                if (newGroup == -1) {
                    System.out.println("Error: el grupo '" + args[1] + "' no existe");
                    return;
                }

                inodeValido = false;
                for (Inode inodeTable : fs.inodeTable) {
                    if (inodeTable != null && inodeTable.getFullName() != null && inodeTable.getFullName().equals(args[2])) {
                        inodeActualizado = inodeTable;
                        if (state.currentUserId == 0 || state.currentUserId == inodeTable.ownerId) {
                            inodeValido = true;
                            inodeTable.groupId = newGroup;
                            asignado = true;
                            break;
                        }
                    }
                }

                if (!inodeValido) {
                    System.out.println("Error: solo root o el dueño puede cambiar el grupo");
                    return;
                }

                if (asignado) {
                    try {
                        fs.diskManager.writeInode(inodeActualizado, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
                        System.out.println("Éxito: el grupo de '" + args[2] + "' ahora es '" + args[1] + "'");
                    } catch (IOException ex) {
                        System.out.println("Error al escribir en disco: " + ex.getMessage());
                    }
                    return;
                }

                System.out.println("Error: archivo/directorio '" + args[2] + "' inexistente");
                break;

            case 4:

                if (!args[1].equals("-R")) {
                    System.out.println("Error uso: chgrp groupname filename / chgrp groupname directory / chgrp -R groupname directory");
                    return;
                }
                newGroup = -1;
                for (Group group : fs.groupTable) {
                    if (group != null && group.groupName != null && group.groupName.equals(args[2])) {
                        newGroup = group.groupId;
                        break;
                    }
                }
                if (newGroup == -1) {
                    System.out.println("Error: el grupo '" + args[2] + "' no existe");
                    return;
                }

                inodeValido = false;
                boolean esDir = false;
                for (Inode inodeTable : fs.inodeTable) {
                    if (inodeTable != null && inodeTable.getFullName() != null && inodeTable.getFullName().equals(args[3])) {
                        if (inodeTable.type.equals(Inode.DIR)) {
                            esDir = true;
                            if (state.currentUserId == 0 || state.currentUserId == inodeTable.ownerId) {
                                inodeActualizado = inodeTable;
                                inodeValido = true;
                                asignado    = true;
                                break;
                            }
                        }
                    }
                }

                if (!esDir) {
                    System.out.println("Error: '" + args[3] + "' no es un directorio, use chgrp sin -R");
                    return;
                }

                if (!inodeValido) {
                    System.out.println("Error: solo root o el dueño puede cambiar el grupo");
                    return;
                }

                cambiarGrupoRecursivo(inodeActualizado, newGroup);

                if (asignado) {
                    System.out.println("Éxito: el grupo de '" + args[3] + "' y su contenido ahora es '" + args[2] + "'");
                    return;
                }

                System.out.println("Error: directorio '" + args[3] + "' inexistente");
                break;
        }
    }

    private void cambiarGrupoRecursivo(Inode inode, int newGroup) {
        inode.groupId = newGroup;
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
                    cambiarGrupoRecursivo(hijo, newGroup);
                }
            }
        }
    }
}