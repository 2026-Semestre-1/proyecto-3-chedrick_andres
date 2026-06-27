package comandos;

import fileSystem.FileSystem;

public class InfoFSCommand implements Command {

    private FileSystem fs;

    public InfoFSCommand(FileSystem fs) {
        this.fs = fs;
    }

    @Override
    public void execute(String[] args) {
        fs.superblock.print();
    }
}