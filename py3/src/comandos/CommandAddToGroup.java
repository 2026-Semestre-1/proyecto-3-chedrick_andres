/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package comandos;

import fileSystem.FileSystem;
import java.io.IOException;
import nucleo.Group;
import nucleo.User;
 
public class CommandAddToGroup implements Command {

    public FileSystem fs;
    public ShellState state;

    public CommandAddToGroup(FileSystem fs, ShellState state) {
        this.fs    = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {

        
        if (args.length != 3) {
            System.out.println("Uso: addtogroup groupname username");
            return;
        }

        
        if (state.currentUserId != 0) {
            System.out.println("Error: solo root puede agregar usuarios a grupos");
            return;
        }

        String groupName = args[1];
        String username  = args[2];

        
        Group targetGroup = null;
        for (int i = 0; i < fs.groupCount; i++) {
            if (fs.groupTable[i] != null && fs.groupTable[i].groupName.equals(groupName)) {
                targetGroup = fs.groupTable[i];
                break;
            }
        }

        if (targetGroup == null) {
            System.out.println("Error: el grupo '" + groupName + "' no existe");
            return;
        }

        
        User targetUser = null;
        for (int i = 0; i < fs.userCount; i++) {
            if (fs.userTable[i] != null && fs.userTable[i].username.equals(username)) {
                targetUser = fs.userTable[i];
                break;
            }
        }

        if (targetUser == null) {
            System.out.println("Error: el usuario '" + username + "' no existe");
            return;
        }

        
        if (targetGroup.isMember(targetUser.userId)) {
            System.out.println("Error: el usuario '" + username + "' ya pertenece al grupo '" + groupName + "'");
            return;
        }

        
        if (!targetGroup.addMember(targetUser.userId)) {
            return;
        }
        targetUser.groupId = targetGroup.groupId;

        
        try {
            fs.diskManager.writeGroup(targetGroup, fs.superblock.groupTableOffset, Group.GROUP_SIZE);
            fs.diskManager.writeUser(targetUser, fs.superblock.userTableOffset, User.USER_SIZE);
            System.out.println("Usuario '" + username + "' agregado al grupo '" + groupName + "' correctamente");
        } catch (IOException e) {
            System.out.println("Error al guardar en disco: " + e.getMessage());
        }
    }
}