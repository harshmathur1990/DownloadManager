package com.download;

import org.apache.commons.io.FilenameUtils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * Created by harshmathur on 02/03/17.
 */
public class Utilities {
    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures, Executor service) {
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApplyAsync(v ->
                futures.stream().
                        map(future -> future.join()).
                        collect(Collectors.<T>toList()),
                service
        );
    }

    public static List<Integer> getRange(Integer first, Integer count) {
        List<Integer> ints = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ints.add(first + i);
        }
        return ints;
    }

    public static String getFileName(URL url, String connectionNumber, String saveDir) {
        try {
            return saveDir+"/"+ URLDecoder.decode(FilenameUtils.getName(url.getPath()), "UTF-8")+connectionNumber;
        } catch (UnsupportedEncodingException e) {
            return saveDir+"/"+ FilenameUtils.getName(url.getPath())+connectionNumber;
        }
    }
}
