/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package comandos;

import fileSystem.FileSystem;
import nucleo.Group;
import java.io.IOException;
public class CommandGroupAdd implements Command {

    private FileSystem fs;
    private ShellState state;

    public CommandGroupAdd(FileSystem fs, ShellState state) {
        this.fs    = fs;
        this.state = state;
    }

    @Override
    public void execute(String[] args) {

        // 1. Validar argumentos
        if (args.length < 2) {
            System.out.println("Uso: groupadd group_name");
            return;
        }

        // 2. Solo root puede crear grupos
        if (state.currentUserId != 0) {
            System.out.println("Error: solo root puede crear grupos");
            return;
        }

        String groupName = args[1];

        // 3. Verificar límite de grupos
        if (fs.groupCount >= Group.MAX_GROUPS) {
            System.out.println("Error: se alcanzó el límite máximo de grupos (" + Group.MAX_GROUPS + ")");
            return;
        }

        // 4. Verificar que el nombre no exista ya
        for (int i = 0; i < fs.groupCount; i++) {
            if (fs.groupTable[i] != null && fs.groupTable[i].groupName.equals(groupName)) {
                System.out.println("Error: el grupo '" + groupName + "' ya existe");
                return;
            }
        }

        // 5. Asignar groupId (el siguiente disponible)
        int newGroupId = fs.groupCount;

        // 6. Crear el GroupEntry
        Group newGroup = new Group();
        newGroup.init(newGroupId, groupName);

        // 7. Guardar en tabla en memoria
        fs.groupTable[newGroupId] = newGroup;
        fs.groupCount++;

        // 8. Persistir en disco
        try {
            fs.diskManager.writeGroup(newGroup, fs.superblock.groupTableOffset, Group.GROUP_SIZE);
            fs.diskManager.writeSuperBlock(fs.superblock);
            System.out.println("Grupo '" + groupName + "' creado correctamente (groupId=" + newGroupId + ")");
        } catch (IOException e) {
            System.out.println("Error al guardar grupo en disco: " + e.getMessage());
        }
    }
}