package com.download;
import org.apache.commons.cli.*;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by harshmathur on 17/02/17.
 */
public class NewMultipleDownloadManager {

    public static void main(String args[]) {
        Options options = new Options();

        Option parallelism = new Option("j", "no-of-threads", true, "Parallelism");
        parallelism.setRequired(false);
        options.addOption(parallelism);

        Option urlList = new Option("f", "urls", true, "URLs in comma separated format");
        urlList.setRequired(true);
        options.addOption(urlList);

        Option maxNUmberOfConnections = new Option("x", "max-number-of-connections", true, "Maximum Number of Connections per URL");
        maxNUmberOfConnections.setRequired(true);
        options.addOption(maxNUmberOfConnections);

        Option saveDirOption = new Option("d", "save-dir", true, "Directory to Save files");
        saveDirOption.setRequired(true);
        options.addOption(saveDirOption);

        Option forceFresh = new Option("k", "fresh", false, "Force The Manager to Download all parts again, don't resume");
        forceFresh.setRequired(false);
        options.addOption(forceFresh);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
            return;
        }

        Integer numberOfThreads = Integer.parseInt(cmd.getOptionValue("no-of-threads"));
        List<String> urls = new ArrayList<String>(Arrays.asList(cmd.getOptionValue("urls").split(",")));
        Integer maxNumberOfConnections = Integer.parseInt(cmd.getOptionValue("max-number-of-connections"));
        String saveDir = cmd.getOptionValue("save-dir");
        Boolean resume = !cmd.hasOption("fresh");

        final ExecutorService service =  Executors.newFixedThreadPool(numberOfThreads);

        List<CompletableFuture<URLDownloadStatus>> downLoadStatus = urls.stream()
                .map(url -> {
                    return CompletableFuture.supplyAsync(() -> {
                        return new NewURLDownloadManager(
                                url, saveDir, maxNumberOfConnections, resume, service).startDownload();

                    }, service);
                })
                .map(urlDownloadFuture -> urlDownloadFuture.join())
                        .collect(Collectors.toList());

        CompletableFuture<List<URLDownloadStatus>> urlDownloadStatusAll = Utilities.sequence(downLoadStatus, service);
        CompletableFuture<List<URLDownloadStatus>> urlDownloadStatusesList = urlDownloadStatusAll.thenApplyAsync(urlDownloadStatuses -> urlDownloadStatuses.stream()
        .map(urlDownloadStatus -> printStatus(urlDownloadStatus))
                .collect(Collectors.toList())
        );

        urlDownloadStatusesList.thenApply(urlDownloadStatuses -> {
            service.shutdown();
            return null;
        });

    }

    public static URLDownloadStatus printStatus(URLDownloadStatus urlStatus) {
        System.out.println("URL: "+urlStatus.getUrl()+" status: "+urlStatus.getStatus()+" speed: "+urlStatus.getSpeed() + " KBps");
        return urlStatus;
    }
}
