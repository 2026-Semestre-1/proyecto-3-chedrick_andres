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
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}