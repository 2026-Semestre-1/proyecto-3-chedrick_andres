/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package nucleo;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author joses
 */
public class Inode implements Serializable{
    //TIPOS
    public static final String FILE = "FILE";
    public static final String DIR = "DIR";
    
    //Estado
    public static final String OPEN = "OPEN";
    public static final String CLOSED = "CLOSED";
    
    // Máximo de bloques por archivo
    public static final int MAX_BLOCKS = 16;

    // Identidad
    public int    id;
    public String name;
    public String extension;
    public String type;        // FILE o DIR

    // Propiedad
    public int ownerId;
    public int groupId;

    // Permisos -> ownerPerm=7, groupPerm=5  /  chmod 75
    public int ownerPerm;
    public int groupPerm;

    // Fechas
    public Date createdAt;
    public Date modifiedAt;

    // Tamaño y estado
    public int    sizeBytes;
    public String status;      // OPEN o CLOSED

    // Bloques donde está el contenido
    public int[] blockIndex;

    // Para enlaces simbólicos (ln)
    public String linkTarget;

    // Para directorios — ids de sus hijos
    public int[] children;
    public int   childCount;

    // Id del directorio padre
    public int parentId;

    public Inode() {}

    public void init(int id, String name, String type, int ownerId, int groupId, int parentId) {
        this.id        = id;
        this.type      = type;
        this.ownerId   = ownerId;
        this.groupId   = groupId;
        this.parentId  = parentId;
        this.status    = CLOSED;
        this.sizeBytes = 0;
        this.createdAt  = new Date();
        this.modifiedAt = new Date();
        this.linkTarget = null;
        this.blockIndex = new int[MAX_BLOCKS];
        this.childCount = 0;

        // formato de nombre 
        if (type.equals(FILE) && name.contains(".")) {
            int dot = name.lastIndexOf(".");
            this.name      = name.substring(0, dot);
            this.extension = name.substring(dot + 1);
        } else {
            this.name      = name;
            this.extension = "";
        }

        // ini bloques en -1 
        for (int i = 0; i < MAX_BLOCKS; i++) {
            blockIndex[i] = -1;
        }

        // Directorios que tienen arreglo de hijos
        if (type.equals(DIR)) {
            this.children = new int[100];
            for (int i = 0; i < 100; i++) children[i] = -1;
        }
    }

    // Asigna los bloques que devuelve el Bitmap
    public void setBlocks(int[] blocks) {
        for (int i = 0; i < blocks.length && i < MAX_BLOCKS; i++) {
            blockIndex[i] = blocks[i];
        }
    }

    // Devuelve solo los bloques que están en uso no -1
    public int[] getUsedBlocks() {
        int count = 0;
        for (int b : blockIndex) if (b != -1) count++;

        int[] used = new int[count];
        int j = 0;
        for (int b : blockIndex) if (b != -1) used[j++] = b;
        return used;
    }

    // Agrega un hijo al directorio
    public void addChild(int inodeId) {
        if (childCount < children.length) {
            children[childCount] = inodeId;
            childCount++;
        }
    }

    // Elimina un hijo del directorio
    public void removeChild(int inodeId) {
        for (int i = 0; i < childCount; i++) {
            if (children[i] == inodeId) {
                children[i] = children[childCount - 1];
                children[childCount - 1] = -1;
                childCount--;
                return;
            }
        }
    }

    // Verifica permisos -> perm: 4=read 2=write 1=execute
    public boolean canRead(int userId, int userGroupId) {
        if (userId == 0) return true; // root puede todo
        if (userId == ownerId)     return (ownerPerm & 4) != 0;
        if (userGroupId == groupId) return (groupPerm & 4) != 0;
        return false;
    }

    public boolean canWrite(int userId, int userGroupId) {
        if (userId == 0) return true;
        if (userId == ownerId)     return (ownerPerm & 2) != 0;
        if (userGroupId == groupId) return (groupPerm & 2) != 0;
        return false;
    }

    public boolean canExecute(int userId, int userGroupId) {
        if (userId == 0) return true;
        if (userId == ownerId)     return (ownerPerm & 1) != 0;
        if (userGroupId == groupId) return (groupPerm & 1) != 0;
        return false;
    }

    // Nombre completo con extensión
    public String getFullName() {
        if (extension == null || extension.isEmpty()) return name;
        return name + "." + extension;
    }

    // Lo que muestra viewFCB
    public void print() {
        System.out.println("----viewFCB ----");
        System.out.println("ID:           " + id);
        System.out.println("Nombre:       " + getFullName());
        System.out.println("Tipo:         " + type);
        System.out.println("Dueño ID:     " + ownerId);
        System.out.println("Grupo ID:     " + groupId);
        System.out.println("Permisos:     " + ownerPerm + "" + groupPerm);
        System.out.println("Tamaño:       " + sizeBytes + " bytes");
        System.out.println("Estado:       " + status);
        System.out.println("Creado:       " + createdAt);
        System.out.println("Modificado:   " + modifiedAt);
        System.out.print("Bloques:      [");
        for (int i = 0; i < MAX_BLOCKS; i++) {
            System.out.print(blockIndex[i]);
            if (i < MAX_BLOCKS - 1) System.out.print(", ");
        }
        System.out.println("]");
        if (linkTarget != null) System.out.println("Link target:  " + linkTarget);
        System.out.println("===================");
    }
    
}
