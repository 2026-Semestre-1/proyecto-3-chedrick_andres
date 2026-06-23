/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package comandos;

import fileSystem.FileSystem;
import nucleo.Inode;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author joses
 */
public class WriteCommand implements Command {

    private FileSystem fs;
    private ShellState state;
    private Scanner scanner;

    public WriteCommand(FileSystem fs, ShellState state, Scanner scanner) {
        this.fs = fs;
        this.state = state;
        this.scanner = scanner;
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: write nombre");
            return;
        }

        int inodeId = fs.buscarInodePorNombre(args[1], state.currentDirId);
        if (inodeId == -1) {
            System.out.println("Error: '" + args[1] + "' no existe");
            return;
        }

        System.out.print("Contenido: ");
        String content = scanner.nextLine();

        writeContent(inodeId, content);
    }

    private void writeContent(int inodeId, String content) {
        try {
            Inode inode = fs.inodeTable[inodeId];

            if (inode == null || !inode.type.equals(Inode.FILE)) {
                System.out.println("Error: no es un archivo válido");
                return;
            }

            byte[] bytes = content.getBytes();
            int blockSize = fs.superblock.blockSize;
            int neededBlocks = (int) Math.ceil((double) bytes.length / blockSize);

            if (neededBlocks > Inode.MAX_BLOCKS) {
                System.out.println("Error: el contenido es muy grande para este archivo");
                return;
            }

            fs.bitmap.free(inode.getUsedBlocks());
            int[] nuevosBloques = fs.bitmap.allocate(neededBlocks);

            if (nuevosBloques == null) {
                System.out.println("Error: no hay espacio en disco");
                return;
            }

            for (int i = 0; i < Inode.MAX_BLOCKS; i++) inode.blockIndex[i] = -1;
            inode.setBlocks(nuevosBloques);
            inode.sizeBytes = bytes.length;

            int offset = 0;
            for (int blockNum : nuevosBloques) {
                int len = Math.min(blockSize, bytes.length - offset);
                byte[] chunk = new byte[blockSize];
                System.arraycopy(bytes, offset, chunk, 0, len);

                fs.diskManager.writeBlock(blockNum, chunk, fs.superblock.dataBlocksOffset, blockSize);
                offset += len;
            }

            fs.diskManager.writeInode(inode, fs.superblock.inodeTableOffset, FileSystem.INODE_SIZE);
            fs.diskManager.writeBitmap(fs.bitmap, fs.superblock.bitmapOffset);
            fs.diskManager.writeSuperBlock(fs.superblock);

            System.out.println("Contenido escrito en el archivo");

        } catch (IOException e) {
            System.out.println("Error al escribir: " + e.getMessage());
        }
    }
}