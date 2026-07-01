package com.mst.service;

import com.mst.dto.GitHubResponseDTO;
import com.mst.exceptions.CsvParseException;
import com.mst.exceptions.GitHubIntegrationException;
import com.mst.exceptions.InvalidFileNameException;
import com.mst.exceptions.LoaderException;
import com.mst.integration.GitHubInte;
import com.mst.model.Loader;
import com.mst.repo.LoaderRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoaderService {

    @Autowired
    private GitHubInte gitHubInte;

    @Autowired
    private CsvParserService csvParserService;

    @Autowired
    private LoaderRepo loaderRepo;

    @Autowired
    private CacheService cacheService;

    public List<Loader> getAll() {
        List<Loader> cachedData = cacheService.getAllDataFromCache();
        if(cachedData != null) {
            return cachedData;
        }
        System.out.println("⚠️ Cache is empty. Loading data from MySQL...");
        List<Loader> data = loaderRepo.findAll();
        cacheService.saveAllToCache(data);
        return data;
    }

    /**
     * פונקציית הסריקה עודכנה לזרוק את השגיאות שלנו החוצה ל-Controller
     */
    @Scheduled(cron = "0 0 * * * *")
    public void scanAuto() {
        System.out.println("⏳ [Auto-Scan] Job woke up! Starting scheduled GitHub scan...");
        try {
            // קריאה לפונקציית הסריקה שעושה את העבודה האמיתית
            String result = scan();

            // הדפסת התוצאה ללוג (במקום להחזיר אותה כ-String החוצה)
            System.out.println("✅ [Auto-Scan] Scan completed successfully. Details: " + result);

        } catch (InvalidFileNameException | CsvParseException e) {
            // שגיאות לוגיות - בעיה בקבצים ספציפיים
            System.err.println("⚠️ [Auto-Scan] Logical data error during scan: " + e.getMessage());

        } catch (GitHubIntegrationException e) {
            // שגיאות רשת - אין גישה לגיטהאב
            System.err.println("❌ [Auto-Scan] Connection error with GitHub: " + e.getMessage());

        } catch (Exception e) {
            // תפיסת מטרייה לכל שגיאה אחרת לא צפויה (כמו בעיית התחברות ל-DB)
            System.err.println("💥 [Auto-Scan] CRITICAL SYSTEM ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("🏁 [Auto-Scan] Job finished and going back to sleep.");
        }
    }
    public String scan() throws LoaderException, GitHubIntegrationException, CsvParseException, InvalidFileNameException {
        LocalDateTime lastScan = loaderRepo.findLastTimestamp();
        List<String> newFilesName = new ArrayList<>();

        if (lastScan != null) {
            System.out.println("Last scan timestamp: " + lastScan);
        }

        int loadedFiles = 0;

        // שליפת תיקיות (עלול לזרוק GitHubIntegrationException)
        List<GitHubResponseDTO> rootContent = gitHubInte.getRootContent();

        for (GitHubResponseDTO folder : rootContent) {
            if (!isDirectory(folder)) continue;

            // שליפת קבצים מהתיקייה (עלול לזרוק GitHubIntegrationException)
            List<GitHubResponseDTO> files = gitHubInte.getFolderContent(folder.getName());

            for (GitHubResponseDTO file : files) {
                if (!isCsvFile(file)) continue;

                // שימוש בשגיאה המותאמת אישית אם חילוץ התאריך נכשל
                LocalDateTime fileDate = extractTimestamp(file.getName());

                if (lastScan != null && !fileDate.isAfter(lastScan)) {
                    // אם הקובץ ישן או שווה לסריקה האחרונה - דלג
                    continue;
                }

                // הפארסר עלול לזרוק CsvParseException שנעביר ל-Controller
                List<Loader> rows = csvParserService.parse(file.getDownload_url());

                if (rows != null && !rows.isEmpty()) {
                    loaderRepo.saveAll(rows);
                    if (!newFilesName.contains(file.getName())) {
                        newFilesName.add(file.getName());
                    }
                    loadedFiles++;
                }
            }
        }

        List<Loader> data = loaderRepo.findAll();
        cacheService.saveAllToCache(data);

        if (loadedFiles == 0) {
            return "No new files to scan";
        }

        return "New files scanned successfully, the files: " + String.join(", ", newFilesName);
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

    /**
     * פונקציה מעודכנת שעוטפת את הלוגיקה שלך ב-Try-Catch למניעת קריסות
     */
    private LocalDateTime extractTimestamp(String fileName) throws InvalidFileNameException {
        try {
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

        } catch (Exception e) {
            // אם הפורמט של השם לא תקין, נזרוק שגיאה ברורה במקום שגיאת סטרניג או תאריך לא ברורה
            throw new InvalidFileNameException("Failed to extract valid timestamp from file name: " + fileName + ". Details: " + e.getMessage());
        }
    }
}