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
public class WhereisCommand implements Command {

    private FileSystem fs;
    private ShellState state;

    public WhereisCommand(FileSystem fs, ShellState state) {
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: whereis nombre");
            return;
        }

        String nombreBuscado = args[1];

        // Buscamos desde (id=0) recorriendo todo el tree
        int encontrado = buscarDesde(0, nombreBuscado);

        if (encontrado == -1) {
            System.out.println("No se encontró '" + nombreBuscado + "'");
        } else {
            System.out.println(construirRuta(encontrado));
        }
    }

    // Recorre recursivamente todo el árbol buscando el nombre
    private int buscarDesde(int dirId, String nombre) {
        Inode dir = fs.inodeTable[dirId];

        for (int i = 0; i < dir.childCount; i++) {
            Inode child = fs.inodeTable[dir.children[i]];
            if (child == null) continue;

            if (child.getFullName().equals(nombre)) {
                return child.id;
            }

            if (child.type.equals(Inode.DIR)) {
                int resultado = buscarDesde(child.id, nombre);
                if (resultado != -1) {
                    return resultado;
                }
            }
        }

        return -1;
    }

    // Misma lógica que PwdCommand, armando la ruta hacia atrás
    private String construirRuta(int inodeId) {
        Inode actual = fs.inodeTable[inodeId];

        if (actual.parentId == -1) {
            return "/";
        }

        StringBuilder ruta = new StringBuilder();

        while (actual.parentId != -1) {
            ruta.insert(0, "/" + actual.getFullName());
            actual = fs.inodeTable[actual.parentId];
        }

        return ruta.toString();
    }
}