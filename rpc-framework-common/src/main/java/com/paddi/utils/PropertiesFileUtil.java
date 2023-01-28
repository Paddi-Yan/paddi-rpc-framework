package com.paddi.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 13:46:09
 */
@Slf4j
public class PropertiesFileUtil {
    private PropertiesFileUtil() { }

    public static Properties readPropertiesFile(String fileName) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if(url != null) {
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;
        try (InputStreamReader reader = new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8)){
            properties = new Properties();
            properties.load(reader);
        }catch(IOException e) {
            log.error("occur exception when read properties file [{}]", fileName);
        }
        return properties;
    }
}