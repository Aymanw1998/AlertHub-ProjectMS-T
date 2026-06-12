package com.mst.controller;

import com.mst.exceptions.CsvParseException;
import com.mst.exceptions.GitHubIntegrationException;
import com.mst.exceptions.InvalidFileNameException;
import com.mst.exceptions.LoaderException;
import com.mst.model.Loader;
import com.mst.service.LoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loader")
public class LoaderController {
    @Autowired
    private LoaderService service;

    @GetMapping("/all-data")
    // GET http://localhost:1000/api/loader/all-data
    public ResponseEntity<List<Loader>> getAllData() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/scan")
    // GET http://localhost:1000/api/loader/scan
    public ResponseEntity<?> scan() {
        try {
            // ניסיון להריץ את הסריקה מול גיטהאב
            String resultMessage = service.scan();
            return ResponseEntity.ok(resultMessage);

        } catch (GitHubIntegrationException e) {
            // שגיאות תקשורת מול גיטהאב (מחזיר 503)
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());

        } catch (CsvParseException | InvalidFileNameException e) {
            // שגיאות בקריאת הקובץ או בשם הקובץ (מחזיר 400)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (LoaderException e) {
            // שגיאות פנימיות של הלוגיקה שלנו (מחזיר 500)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());

        } catch (Exception e) {
            // תפיסת שגיאות בלתי צפויות
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }
}