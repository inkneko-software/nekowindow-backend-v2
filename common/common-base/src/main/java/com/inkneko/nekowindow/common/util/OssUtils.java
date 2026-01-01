package com.inkneko.nekowindow.common.util;

import lombok.AllArgsConstructor;

import java.net.URI;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OssUtils {
    private static final Pattern pattern = Pattern.compile("(http|https)://(.+?)/(.+?)/(.+?)(\\?*)");

    @AllArgsConstructor
    public static class OssLink{
        public String endpoint;
        public String bucket;
        public String key;
    }

    public static OssLink url(String url){
        Matcher matcher = pattern.matcher(url);
        if (matcher.matches()){
            URI uri;
            try{
                uri = URI.create(url);
            }catch (Exception e){
                return null;
            }

            return new OssLink(matcher.group(2), uri.getPath().split("/")[1], uri.getPath().substring(uri.getPath().indexOf("/", 1) + 1));
        }
        return null;
    }
}
