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

    public static void unZip(String zipFile, String outputFolder) throws IOException {

        byte[] buffer = new byte[1024];

        try {

            //create output directory is not exists
            File folder = new File(outputFolder);
            if (!folder.exists()) {
                folder.mkdir();
            }

            //get the zip file content
            ZipInputStream zis =
                    new ZipInputStream(new FileInputStream(zipFile));
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
