/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;

/**
 *
 * @author joses
 */
public class CdCommand implements Command {

    private FileSystem fs;
    private ShellState state;

    public CdCommand(FileSystem fs, ShellState state) {
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: cd nombre | cd ..");
            return;
        }

        String destino = args[1];

        // Caso 1 sube al directorio padre
        if (destino.equals("..")) {
            Inode actual = fs.inodeTable[state.currentDirId];

            if (actual.parentId == -1) {
                System.out.println("Ya estas en la raiz, no se puede subir mas");
                return;
            }

            state.currentDirId = actual.parentId;
            return;
        }

        // Caso 2: entra a un subdirectorio
        int targetId = fs.buscarInodePorNombre(destino, state.currentDirId);

        if (targetId == -1) {
            System.out.println("Error: '" + destino + "' no existe");
            return;
        }

        Inode target = fs.inodeTable[targetId];

        if (!target.type.equals(Inode.DIR)) {
            System.out.println("Error: '" + destino + "' no es un directorio");
            return;
        }

        state.currentDirId = targetId;
    }
}