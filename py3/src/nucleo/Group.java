/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package nucleo;

import java.io.Serializable;
import java.util.Date;

/**
 * Representa un grupo del sistema de archivos.
 * Se serializa y guarda en la sección de grupos del .fs
 */
public class Group implements Serializable {

    public static final int MAX_GROUPS   = 20;  // máximo de grupos permitidos
    public static final int GROUP_SIZE   = 512; // bytes reservados por grupo en disco
    public static final int MAX_MEMBERS  = 50;  // máximo de miembros por grupo

    // Identidad
    public int    groupId;
    public String groupName;

    // Miembros — se guardan los userIds
    public int[] members;
    public int   memberCount;

    // Fechas
    public Date createdAt;

    // Estado
    public boolean active;

    public Group() {}

    public void init(int groupId, String groupName) {
        this.groupId     = groupId;
        this.groupName   = groupName;
        this.members     = new int[MAX_MEMBERS];
        this.memberCount = 0;
        this.createdAt   = new Date();
        this.active      = true;

        // Inicializar miembros en -1
        for (int i = 0; i < MAX_MEMBERS; i++) {
            members[i] = -1;
        }
    }

    // Agrega un userId al grupo
    public boolean addMember(int userId) {
        if (memberCount >= MAX_MEMBERS) {
            System.out.println("Error: el grupo está lleno");
            return false;
        }
        // Verificar que no esté ya en el grupo
        for (int i = 0; i < memberCount; i++) {
            if (members[i] == userId) {
                System.out.println("Error: el usuario ya pertenece a este grupo");
                return false;
            }
        }
        members[memberCount] = userId;
        memberCount++;
        return true;
    }

    // Elimina un userId del grupo
    public boolean removeMember(int userId) {
        for (int i = 0; i < memberCount; i++) {
            if (members[i] == userId) {
                members[i] = members[memberCount - 1];
                members[memberCount - 1] = -1;
                memberCount--;
                return true;
            }
        }
        return false;
    }

    // Verifica si un userId pertenece al grupo
    public boolean isMember(int userId) {
        for (int i = 0; i < memberCount; i++) {
            if (members[i] == userId) return true;
        }
        return false;
    }

    public void print() {
        System.out.println("---- Group ----");
        System.out.println("ID:       " + groupId);
        System.out.println("Nombre:   " + groupName);
        System.out.println("Miembros: " + memberCount);
        System.out.print("UserIDs:  [");
        for (int i = 0; i < memberCount; i++) {
            System.out.print(members[i]);
            if (i < memberCount - 1) System.out.print(", ");
        }
        System.out.println("]");
        System.out.println("Creado:   " + createdAt);
        System.out.println("===============");
    }
}