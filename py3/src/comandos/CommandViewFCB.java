/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;

public class CommandViewFCB implements Command {

    public FileSystem fs;
    public ShellState state;

    public CommandViewFCB(FileSystem fs, ShellState state) {
        this.fs    = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {

        
        if (args.length != 2) {
            System.out.println("Uso: viewFCB filename");
            return;
        }

        
        Inode encontrado = null;
        for (Inode inode : fs.inodeTable) {
            if (inode != null && inode.getFullName().equals(args[1])) {
                encontrado = inode;
                break;
            }
        }

        
        if (encontrado == null) {
            System.out.println("Error: '" + args[1] + "' no existe");
            return;
        }
        encontrado.print();
    }
}