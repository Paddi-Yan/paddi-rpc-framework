package com.paddi.utils;

/**
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 10:40:26
 */
public class StringUtil {
    public static boolean isBlank(String s) {
        if(s == null || s.length() == 0) {
            return true;
        }
        for(int i = 0; i < s.length(); i++) {
            if(!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
