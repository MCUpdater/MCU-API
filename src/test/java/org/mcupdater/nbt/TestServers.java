package org.mcupdater.nbt;

import org.mcupdater.mojang.nbt.NBTParser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class TestServers {

    public static void main(String[] args) {
        try {
            File serverFile = new File(args[0]);
            byte[] content = Files.readAllBytes(serverFile.toPath());
            NBTParser.RVPair output = NBTParser.parse(content,null);
            System.out.println(output.tag.toString());
            byte[] test = output.tag.toBytes(true);
            int address = 0;
            for (byte value : test) {
                if ((address % 16) == 0) {
                    System.out.printf("%n%08x: ",address);
                }
                System.out.printf("%02x",value);
                if (address % 2 == 1) {
                    System.out.print(" ");
                }
                address++;
            }
            System.out.println();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
