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
    
    public CommandChown(FileSystem fs, ShellState state){
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 3 || args.length > 4 ){
            System.out.println("Error uso: chown username filename / chown username directory / chown -R username directory");
            return;
        }
        Inode inodeActualizado = null;
        boolean asignado = false;
        int newOwner;
        boolean directorioValido;
        switch(args.length){
            case 3:
                newOwner = -1;
                for(User user : fs.userTable){
                    if (user != null && user.username != null && user.username.equals(args[1])){
                        newOwner = user.userId;
                        break;
                    }
                }
                if(newOwner == -1){
                    System.out.println("Error: El usuario ingresado no existe");
                    return;
                }
                directorioValido = false;
                System.out.println(state.currentUserId);
                for (Inode inodeTable : fs.inodeTable) {
                    if(inodeTable != null && inodeTable.name != null){
                        if(inodeTable.getFullName().equals(args[2])){
                            inodeActualizado = inodeTable;
                            System.out.println(inodeTable.ownerId);
                            if(state.currentUserId == 0 || state.currentUserId == inodeTable.ownerId){
                                
                                System.out.println("Si es valido el usuario");
                                directorioValido = true;
                                inodeTable.ownerId = newOwner;
                                asignado = true;
                                break;
                            }
                        }
                    }
                }
                if(!directorioValido){
                    System.out.println("Error: Sólo el usuario root o el dueño del archivo/directorio puede cambiar al dueño");
                    return;
                }
                if (asignado){
                    try {
                        fs.diskManager.writeInode(inodeActualizado, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
                    } catch (IOException ex) {
                        System.out.println("Erro al escribir en disco: " + ex.getMessage());
                    }
                        System.out.println("Éxito: El usuario "+ args[1] + " es el nuevo dueño de "+ args[2]);
                        return;
                }
                System.out.println("Archivo/directorio "+ args[2] +" inexistente...");
                break;
            case 4:
                if(!args[1].equals("-R")){
                    System.out.println("Error uso: chown username filename / chown username directory / chown -R username directory");
                    return;
                }
                newOwner = -1;
                for(User user : fs.userTable){
                    if (user != null && user.username != null && user.username.equals(args[2])){
                        newOwner = user.userId;
                        break;
                    }
                }
                if(newOwner == -1){
                    System.out.println("Error: El usuario ingresado no existe");
                    return;
                }
                
                directorioValido = false;
                boolean esDir = false;
                for (Inode inodeTable : fs.inodeTable) {
                    if(inodeTable != null && inodeTable.name != null){
                        if(inodeTable.getFullName().equals(args[3]) && (inodeTable.type == null ? Inode.DIR == null : inodeTable.type.equals(Inode.DIR))){
                            esDir = true;
                            if(state.currentUserId == 0 || state.currentUserId == inodeTable.ownerId){
                                inodeActualizado = inodeTable;
                                directorioValido = true;
                                asignado = true;
                                break;
                            }
                        }
                    }
                }
                if(!directorioValido){
                    System.out.println("Error: Sólo el usuario root o el dueño del archivo/directorio puede cambiar al dueño");
                    return;
                }
                if(!esDir){
                    System.out.println("Error '-R' se usa con directorios");
                    return;
                }
                cambiarRecursivo(inodeActualizado, newOwner);
                if (asignado){
                    System.out.println("Éxito: El usuario "+ args[2] + " es el nuevo dueño de "+ args[3]);
                    return;
                }
                System.out.println("Archivo/directorio "+ args[3] +" inexistente...");
                break;
            }
        }
        
    private void cambiarRecursivo(Inode inode, int newOwner){
        inode.ownerId = newOwner;
        try {
            fs.diskManager.writeInode(inode, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
        } catch (IOException ex) {
            System.out.println("Erro al escribir en disco: " + ex.getMessage());
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
