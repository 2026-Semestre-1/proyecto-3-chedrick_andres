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
        switch(args.length){
            case 3:
                int newOwner = -1;
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
                int currentDir = state.currentDirId;
                
                
                
                for (Inode inodeTable : fs.inodeTable) {
                    if(inodeTable.name != null){
                        if(inodeTable.name.equals(args[2])){
                            inodeActualizado = inodeTable;
                            if(state.currentUserId == 0 || state.currentUserId == inodeTable.ownerId){
                                inodeTable.ownerId = newOwner;
                                asignado = true;
                                break;
                            }else{
                                System.out.println("Error: Sólo el usuario root o el dueño del archivo/directorio puede cambiar al dueño");
                                return;
                            }
                        }
                    }
                }
                break;
            case 4:
                
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
    }
        
}
