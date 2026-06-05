package com.mst.service;

import com.mst.dto.github.GitHubContentDTO;
import com.mst.integration.GitHubInte;
import com.mst.model.Loader;
import com.mst.repo.LoaderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class LoaderService {
    @Autowired
    private  GitHubInte                                                                                                                                                                                                                 gitHubInte;
    @Autowired
    private  CsvParserService csvParserService;
    @Autowired
    private  LoaderRepo loaderRepo;

    public String scan() {
        LocalDateTime lastScan = loaderRepo.findLastTimestamp();
        if (lastScan != null) {
            System.out.println(lastScan);
        }

        int loadedFiles = 0;
        int loadedRows = 0;

        List<GitHubContentDTO> rootContent = gitHubInte.getRootContent();

        for (GitHubContentDTO folder : rootContent) {

            if (!isDirectory(folder)) {
                continue;
            }

            List<GitHubContentDTO> files = gitHubInte.getFolderContent(folder.getName());

            for (GitHubContentDTO file : files) {

                if (!isCsvFile(file)) {
                    continue;
                }

                try {
                    if(lastScan != null) {
                        LocalDateTime fileDate = extractTimestamp(file.getName());

                        if(lastScan.isAfter(fileDate)) continue;
                    }
                    List<Loader> rows = csvParserService.parse(file.getDownload_url());
                    loaderRepo.saveAll(rows);

                    loadedFiles++;
                    loadedRows += rows.size();
                } catch(Exception e) {
                    System.out.println("Error for parse: " + e.getMessage());
                }
            }
        }

        return "Scan completed. Loaded files: "
                + loadedFiles
                + ", Loaded rows: "
                + loadedRows;
    }

    private boolean isDirectory(GitHubContentDTO item) {
        return item != null && "dir".equals(item.getType());
    }

    private boolean isCsvFile(GitHubContentDTO item) {
        return item != null
                && "file".equals(item.getType())
                && item.getName() != null
                && item.getName().endsWith(".csv");
    }

    private LocalDateTime extractTimestamp(String fileName) {


        String fileNameCleanType = fileName.replace(".csv", "");

        String datePart = fileNameCleanType.substring(fileNameCleanType.indexOf('_') + 1);

        String[] dateTimeStrArr = datePart.split("T");

        String dateStr = dateTimeStrArr[0].replace("_", "-");
        String timeStr = dateTimeStrArr[1].replace("_", ":");

        return LocalDateTime.of(LocalDate.parse(dateStr), LocalTime.parse(timeStr));

    }
}