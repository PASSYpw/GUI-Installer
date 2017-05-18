package pw.passy.installer;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by liz3 on 17.05.2017.
 */
public class Utils {

    public static void unZip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[1024];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
    public static void downloadFile(URL url, File f) throws Exception {
        if (!f.exists()) {
            f.createNewFile();
        }

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.addRequestProperty("user-agent", " Mozilla/5.0 (Windows NT x.y; rv:10.0) Gecko/20100101 Firefox/10.0");
        conn.connect();
        BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
        FileOutputStream fis = new FileOutputStream(f);
        byte[] buffer = new byte[1024];


        int count1;
        while ((count1 = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, count1);
        }

        fis.close();
        bis.close();
    }
}
