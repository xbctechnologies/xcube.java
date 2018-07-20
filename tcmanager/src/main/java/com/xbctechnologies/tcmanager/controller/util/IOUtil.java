package com.xbctechnologies.tcmanager.controller.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;

public class IOUtil {

    public static String getContent(MultipartFile file) {
        String encoding = "UTF-8";
        final int bufferSize = 2048;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(file.getInputStream());
            Reader in = new InputStreamReader(inputStream, encoding);
            for (; ; ) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    inputStream = null;
                }
            }
        }
    }

    public static String getKeyJsonConvertString(String filecontent) {
        filecontent =
                "{\n" +
                        "  \"result\": \n" +
                        filecontent
                        + "  \n" + "}";
        return filecontent;
    }
}
