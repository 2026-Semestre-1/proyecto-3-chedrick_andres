package comandos;

import fileSystem.FileSystem;
import nucleo.User;
public class CommandWhoami implements Command {

    private FileSystem fs;
    private ShellState state;

    public CommandWhoami(FileSystem fs, ShellState state) {
        this.fs    = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {
        User currentUser = fs.userTable[state.currentUserId];

        if (currentUser == null) {
            System.out.println("Error: no se encontró el usuario activo");
            return;
        }

        System.out.println("username:  " + currentUser.username);
        System.out.println("Full name: " + currentUser.fullName);
    }
}