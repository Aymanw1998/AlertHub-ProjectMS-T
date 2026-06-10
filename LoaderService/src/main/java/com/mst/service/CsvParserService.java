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

    public List<Loader> parse(String downloadUrl) throws CsvParseException {
        List<Loader> result = new ArrayList<>();
        InputStream stream = null;
        CSVReader reader = null;
        try {
            stream = new URL(downloadUrl).openStream();
            reader = new CSVReader(new InputStreamReader(stream));
            String[] row;
            boolean firstLine = true;
            while ((row = reader.readNext()) != null) {
                //Skip first line (the headers)
                if (firstLine) {
                    firstLine = false;
                    continue;
                }
                Loader loader = mapRow(row);
                result.add(loader);

            }
            return result;
        } catch (Exception e) {
            throw new CsvParseException("Cannot parse CSV file: " + downloadUrl);
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if(stream != null){
                    stream.close();
                }
            } catch(Exception ex) {
                System.out.println("Failed to close stream: " + ex.getMessage());
            }
        }
    }

    private Loader mapRow(String[] row) {
        Loader loader = new Loader();
        loader.setTimestamp(LocalDateTime.now());
        loader.setOwner_id(Long.valueOf(row[1]));
        loader.setProject(row[2]);
        loader.setTag(row[3]);
        loader.setLabel(Label.valueOf(row[4])); //row[] -> string
        loader.setDeveloper_id(row[5]);
        loader.setTask_number(row[6]);
        loader.setEnvironment(Environment.valueOf(row[7]));
        loader.setUser_story(row[8]);
        loader.setTask_point(Integer.valueOf(row[9]));
        loader.setSprint(row[10]);

        return loader;
    }
}
