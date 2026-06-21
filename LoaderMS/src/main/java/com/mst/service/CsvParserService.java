package com.mst.service;

import com.mst.exceptions.CsvParseException;
import com.mst.model.Environment;
import com.mst.model.Label;
import com.mst.model.Loader;
import com.opencsv.CSVReader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvParserService {

    // הפונקציה חזרה לקבל רק את ה-URL כפי שהיה במקור
    public List<Loader> parse(String downloadUrl) throws CsvParseException {
        List<Loader> result = new ArrayList<>();
        InputStream stream = null;
        CSVReader reader = null;

        try {
            stream = new URL(downloadUrl).openStream();
            reader = new CSVReader(new InputStreamReader(stream));
            String[] row;
            boolean firstLine = true;
            int rowNumber = 1; // המונה שלנו בשביל הודעות השגיאה

            while ((row = reader.readNext()) != null) {
                // דילוג על שורת הכותרות
                if (firstLine) {
                    firstLine = false;
                    rowNumber++;
                    continue;
                }

                Loader loader = mapRow(row, rowNumber);
                result.add(loader);
                rowNumber++;
            }
            return result;

        } catch (CsvParseException e) {
            // אם זרקנו שגיאת CSV מותאמת אישית, נזרוק אותה הלאה
            throw e;
        } catch (Exception e) {
            throw new CsvParseException("Cannot parse CSV file: " + downloadUrl + " | Error: " + e.getMessage());
        } finally {
            // חזרה למבנה ה-finally המוכר שלך לסגירת הקשרים
            try {
                if (reader != null) {
                    reader.close();
                }
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception ex) {
                System.out.println("Failed to close stream: " + ex.getMessage());
            }
        }
    }

    private Loader mapRow(String[] row, int rowNumber) throws CsvParseException {
        Loader loader = new Loader();

        try {
            loader.setTimestamp(LocalDateTime.now()); // חזרנו לזמן הריצה של עכשיו!
            loader.setOwner_id(row[1] == null || row[1].isBlank() ? 0L : Long.valueOf(row[1].trim()));
            loader.setProject(row[2]);
            loader.setTag(row[3]);
            loader.setLabel(Label.fromString(row[4]));
            loader.setDeveloper_id(row[5]);
            loader.setTask_number(row[6]);
            loader.setEnvironment(Environment.fromString(row[7].trim()));
            loader.setUser_story(row[8]);
            loader.setTask_point(row[9] == null || row[9].isBlank() ? 0 :Integer.valueOf(row[9].trim()));
            loader.setSprint(row[10]);

            // הרצת וולידציה על הנתונים עם מספר השורה
            validateLoaderData(loader, rowNumber);

            return loader;

        } catch (IllegalArgumentException e) {
            // תופס שגיאות של המרת טקסט למספר או המרת טקסט ל-Enum לא מוכר
            throw new CsvParseException("Invalid data format in CSV at row " + rowNumber + " (bad number or enum). Details: " + e.getMessage());
        }
    }

    private void validateLoaderData(Loader loader, int rowNumber) throws CsvParseException {
        if (loader.getProject() == null || loader.getProject().trim().isEmpty()) {
            throw new CsvParseException("Missing project name in CSV at row: " + rowNumber);
        }

        if (loader.getTask_number() == null || loader.getTask_number().trim().isEmpty()) {
            throw new CsvParseException("Missing task number in CSV at row: " + rowNumber);
        }

        if (loader.getDeveloper_id() == null || loader.getDeveloper_id().trim().isEmpty()) {
            throw new CsvParseException("Missing developer ID in CSV at row: " + rowNumber);
        }

        if (loader.getTask_point() != null && loader.getTask_point() < 0) {
            throw new CsvParseException("Task points cannot be negative in CSV at row: " + rowNumber);
        }
    }
}