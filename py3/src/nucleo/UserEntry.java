package nucleo;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.Date;

/**
 * Representa un usuario del sistema de archivos.
 * Se serializa y guarda en la sección de usuarios del .fs
 */
public class UserEntry implements Serializable {

    public static final int MAX_USERS = 50; // máximo de usuarios permitidos
    public static final int USER_SIZE  = 512; // bytes reservados por usuario en disco

    // Identidad
    public int    userId;
    public String username;
    public String fullName;

    // Seguridad
    public String passwordHash; // nunca se guarda en texto plano

    // Grupo y permisos
    public int     groupId;
    public boolean isRoot;

    // Directorio home
    public String homePath;

    // Fechas
    public Date createdAt;
    public Date lastLogin;

    // Estado
    public boolean active;

    public UserEntry() {}

    public void init(int userId, String username, String fullName, String password, int groupId, boolean isRoot) {
        this.userId       = userId;
        this.username     = username;
        this.fullName     = fullName;
        this.passwordHash = hashPassword(password);
        this.groupId      = groupId;
        this.isRoot       = isRoot;
        this.homePath     = isRoot ? "/root" : "/home/" + username;
        this.createdAt    = new Date();
        this.lastLogin    = null;
        this.active       = true;
    }

    // SHA-256 simple para hashear la contraseña
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            // fallback muy básico si falla SHA-256
            return Integer.toHexString(password.hashCode());
        }
    }

    // Verifica si la contraseña ingresada es correcta
    public boolean checkPassword(String password) {
        return this.passwordHash.equals(hashPassword(password));
    }

    public void print() {
        System.out.println("---- viewUser ----");
        System.out.println("ID:         " + userId);
        System.out.println("Username:   " + username);
        System.out.println("Nombre:     " + fullName);
        System.out.println("Grupo ID:   " + groupId);
        System.out.println("Es root:    " + isRoot);
        System.out.println("Home:       " + homePath);
        System.out.println("Creado:     " + createdAt);
        System.out.println("Último login: " + (lastLogin != null ? lastLogin : "nunca"));
        System.out.println("Activo:     " + active);
        System.out.println("==================");
    }
}