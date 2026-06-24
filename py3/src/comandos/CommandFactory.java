package comandos;

import fileSystem.FileSystem;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CommandFactory {

    private Map<String, Command> comandos;

    public CommandFactory(FileSystem fs, ShellState state, Scanner scanner) {
        comandos = new HashMap<>();

        comandos.put("mkdir", new MkdirCommand(fs, state));
        comandos.put("touch", new TouchCommand(fs, state));
        comandos.put("ls",    new LsCommand(fs, state));
        comandos.put("rm",    new RmCommand(fs, state));
        comandos.put("cat",   new CatCommand(fs, state));
        comandos.put("write", new WriteCommand(fs, state, scanner));
        comandos.put("pwd", new PwdCommand(fs, state));
        comandos.put("cd",  new CdCommand(fs, state));
        comandos.put("mv", new MvCommand(fs, state));
        comandos.put("whereis", new WhereisCommand(fs, state));
    }
    

    public Command getCommand(String nombre) {
        return comandos.get(nombre);
    }
}