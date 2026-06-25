package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;

public class PwdCommand implements Command {

    private FileSystem fs;
    private ShellState state;

    public PwdCommand(FileSystem fs, ShellState state) {
        this.fs = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        System.out.println(construirRuta(state.currentDirId));
    }

    private String construirRuta(int dirId) {
        Inode dir = fs.inodeTable[dirId];

        if (dir.parentId == -1) {
            return "/"; // La raíz
        }

        StringBuilder ruta = new StringBuilder();
        Inode actual = dir;

        while (actual.parentId != -1) {
            ruta.insert(0, "/" + actual.getFullName());
            actual = fs.inodeTable[actual.parentId];
        }

        return ruta.toString();
    }
}