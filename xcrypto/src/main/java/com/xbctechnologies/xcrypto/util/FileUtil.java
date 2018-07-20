package com.xbctechnologies.xcrypto.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class FileUtil {

    // encrypted key json file write method
    public static void writeEncKeyJsonFile(String path, String[] result) throws IllegalStateException {
        if (result == null && result.length != 2) {
            throw new IllegalArgumentException("newAccount fail. input params check.");
        }
        String content = result[0];
        String address = result[1];

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTimeInMillis(System.nanoTime());
        Date date = calendar.getTime();
        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm-ss.SSSSSSSSS")
                .format(date);
        String filename = "UTC--" + formatted + "Z--" + address;

        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(path + File.separator + filename);
            bw = new BufferedWriter(fw);
            bw.write(content);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e.toString());
        } finally {
            try {
                if (bw != null)
                    bw.close();
                if (fw != null)
                    fw.close();
            } catch (IOException ex) {
                bw = null;
                fw = null;
            }
        }
    }
}
