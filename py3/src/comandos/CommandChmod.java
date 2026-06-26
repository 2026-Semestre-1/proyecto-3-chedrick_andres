/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comandos;

import fileSystem.FileSystem;
import java.io.IOException;
import nucleo.Inode;

/**
 *
 * @author chedr
 */
public class CommandChmod implements Command {
    public FileSystem fs;
    public ShellState state;
    
    public CommandChmod(FileSystem fs, ShellState state){
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if(!(args.length == 3)){
            System.out.println("Uso chmod: chmod n1n2 dir/arch");
            return;
        }
        if(!(args[1].length() == 2)){
            System.out.println("Error: ingresee números válidos");
            return;
        }
        int ownerPerm = Character.getNumericValue(args[1].charAt(0));
        int groupPerm = Character.getNumericValue(args[1].charAt(1));
        
        if(!(ownerPerm >= 0 && ownerPerm <= 7 ) || !(groupPerm >= 0 && groupPerm <= 7 )){
            System.out.println("Error: ingresee números válidos");
            return;
        }
        Inode inodeActualizado = null;
        boolean directorioValido = false;
        for (Inode inodeTable : fs.inodeTable) {
            if(inodeTable != null && inodeTable.name != null){
                if(inodeTable.name.equals(args[2])){
                    inodeActualizado = inodeTable;
                    if(state.currentUserId == 0 || state.currentUserId == inodeTable.ownerId){
                        directorioValido = true;
                        break;
                    }
                }
            }
        }
        if(!directorioValido){
            System.out.println("Error: Sólo el usuario root o el dueño del archivo/directorio puede cambiar los permisos");
            return;
        }
        if(inodeActualizado == null){
            System.out.println("Error: archivo/directorio '" + args[2] + "' inexistente");
            return;
        }
        inodeActualizado.ownerPerm = ownerPerm;
        inodeActualizado.groupPerm = groupPerm;

        try {
            fs.diskManager.writeInode(inodeActualizado, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            System.out.println("Éxito: permisos de '" + args[2] + "' cambiados a " + args[1]);
        } catch (IOException ex) {
            System.out.println("Error al escribir en disco: " + ex.getMessage());
        }
        
        
    }
    
    
}
