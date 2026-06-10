package com.mst.service;

import com.mst.dto.github.GitHubResponseDTO;
import com.mst.exceptions.GitHubIntegrationException;
import com.mst.exceptions.LoaderException;
import com.mst.integration.GitHubInte;
import com.mst.model.Loader;
import com.mst.repo.LoaderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoaderService {
    @Autowired
    private  GitHubInte                                                                                                                                                                                                                 gitHubInte;

    @Autowired
    private  CsvParserService csvParserService;

    @Autowired
    private  LoaderRepo loaderRepo;

    public List<Loader> getAll() {
        return loaderRepo.findAll();
    }

    public String scan() {
        LocalDateTime lastScan = loaderRepo.findLastTimestamp();
        List<String> newFilesName = new ArrayList<>();
        if(lastScan != null) System.out.println(lastScan);
        int loadedFiles = 0;
        List<GitHubResponseDTO> rootContent = gitHubInte.getRootContent();
        for(GitHubResponseDTO folder : rootContent) {
            if (!isDirectory(folder)) continue;
            List<GitHubResponseDTO> files = gitHubInte.getFolderContent(folder.getName());
            for(GitHubResponseDTO file : files) {
                if (!isCsvFile(file)) continue;
                try {
                    if(lastScan != null) {
                        LocalDateTime fileDate = extractTimestamp(file.getName());
                        if(lastScan.isAfter(fileDate)) continue;
                    }
                    List<Loader> rows = csvParserService.parse(file.getDownload_url());
                    loaderRepo.saveAll(rows);
                    if(!newFilesName.contains(file.getName()))
                        newFilesName.addLast(file.getName());
                    loadedFiles++;
                } catch(Exception e) {
                    throw new LoaderException("Error while parsing file: " + file.getName());
                }
            }
        }
        if(loadedFiles == 0)
            return "No new files to scan";
        return "New files scanned successfully, the  files: " + String.join(", ", newFilesName);
    }

    private boolean isDirectory(GitHubResponseDTO item) {
        return item != null && "dir".equals(item.getType());
    }

    private boolean isCsvFile(GitHubResponseDTO item) {
        return item != null
                && "file".equals(item.getType())
                && item.getName() != null
                && item.getName().endsWith(".csv");
    }

    private LocalDateTime extractTimestamp(String fileName) {
        //: jira_2024_08_22T13_30_00
        String fileNameCleanType = fileName.replace(".csv", "");
        String datePart = fileNameCleanType.substring(fileNameCleanType.indexOf('_') + 1);
        //2024_08_22T13_30_00
        String[] dateTimeStrArr = datePart.split("T");
        //["date"],["time"]
        String dateStr = dateTimeStrArr[0].replace("_", "-");
        String timeStr = dateTimeStrArr[1].replace("_", ":");
        //2024-08-22
        //13:30:00
        return LocalDateTime.of(LocalDate.parse(dateStr), LocalTime.parse(timeStr));

    }
}