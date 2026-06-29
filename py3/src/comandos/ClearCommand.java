/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package comandos;

/**
 *
 * @author joses
 */
public class ClearCommand implements Command {
    @Override
    public void execute(String[] args) {
        try {
            new ProcessBuilder("clear")
                .inheritIO()
                .start()
                .waitFor();
        } catch (Exception e) {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }
}