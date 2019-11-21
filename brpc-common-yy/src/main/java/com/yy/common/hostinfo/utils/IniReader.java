package com.yy.common.hostinfo.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class IniReader {

    private static final long LAST_MODIFIED = -1;

    private static Map<String, String> sections = new HashMap<>();

    /**
     * 构造函数
     *
     * @param filename
     * @throws IOException
     */
    public IniReader(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            return;
        }
        if (LAST_MODIFIED != file.lastModified()) {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            read(reader);
            reader.close();
        }
    }

    /**
     * 读取文件
     *
     * @param reader
     * @throws IOException
     */
    protected void read(BufferedReader reader) throws IOException {
        Map<String, String> resultTemp = new HashMap<>();
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.matches(".*=.*")) {
                int i = line.indexOf('=');
                String name = line.substring(0, i);
                String value = line.substring(i + 1);
                resultTemp.put(name, value);
            }
        }
        if (resultTemp.size() != 0) {
            sections = resultTemp;
        }
    }

    /**
     * 获取值
     *
     * @param key
     * @return
     */
    public String getValue(String key) {
        return sections.get(key);
    }

    public String getValue(String key, String defaultValue) {
        String value = sections.get(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 是否包含key
     *
     * @param key
     * @return
     */
    public boolean containsKey(String key) {
        return sections.containsKey(key);
    }
}  