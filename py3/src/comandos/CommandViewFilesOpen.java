/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;
public class CommandViewFilesOpen implements Command {

    public FileSystem fs;
    public ShellState state;

    public CommandViewFilesOpen(FileSystem fs, ShellState state) {
        this.fs    = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        int total = 0;

        for (Inode inode : fs.inodeTable) {
            if (inode != null && Inode.OPEN.equals(inode.status)) {
                total++;
            }
        }

        System.out.println("Total de archivos abiertos: " + total);

        if (total == 0) return;

        System.out.println("-----------------------------");
        for (Inode inode : fs.inodeTable) {
            if (inode != null && Inode.OPEN.equals(inode.status)) {
                System.out.println("- " + inode.getFullName() + "  (dueño ID: " + inode.ownerId + ")");
            }
        }
    }
}