package net.pingpong.imstracer;

import java.io.File;

public class FileReader {

    static void examineFiles(String[] args, int startIndex, ImsReader.ImsCallback callback) throws Exception {
        for (int i = startIndex; i < args.length; i++) {
            File fileOrDir = new File(args[i]);
            if (!(fileOrDir.isFile() || fileOrDir.isDirectory())) {
                System.err.println("Invalid file or directory: " + fileOrDir);
                System.exit(1);
            }
            examineFileOrDir(fileOrDir, callback);
        }
    }

    private static void examineFileOrDir(File file, ImsReader.ImsCallback callback) throws Exception {
        if (file.isDirectory()) {
            for (File subFile : file.listFiles()) {
                examineFileOrDir(subFile, callback);
            }
        } else if (file.isFile() && (file.getName().endsWith(".xml") || file.getName().endsWith(".xml.bz2"))) {
            try {
                ImsReader.parseFile(file, callback);
            } catch (Exception e) {
                System.err.println("Error parsing " + file.getAbsolutePath() + " - skipping");
            }
        }
    }

}
