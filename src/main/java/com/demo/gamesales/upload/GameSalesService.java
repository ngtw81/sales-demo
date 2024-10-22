package com.demo.gamesales.upload;

import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
public class GameSalesService {
    private static final String TYPE = "text/csv";

    public boolean hasCsvFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    private static final String VALID_CSV_HEADERS = "[id, game_no, game_name, game_code, type, cost_price, percentage, sale_price, sale_date]";

    public boolean isValidCSVHeaders(String[] headers) {
        return Arrays.toString(headers).equalsIgnoreCase(VALID_CSV_HEADERS);
    }

    public List<String[]> extractRecordsFromCSV(MultipartFile multipartFile) throws IOException {
        try (Reader inputReader = new InputStreamReader(multipartFile.getInputStream(), "UTF-8")) {
            CsvParser parser = new CsvParser(new CsvParserSettings());
            return parser.parseAll(inputReader);
        } catch (IOException e) {
            throw e;
        }
    }

    public List<GameSaleDTO> extractRecordsFromCSVToBeans(MultipartFile multipartFile) throws IOException {

        try (Reader inputReader = new InputStreamReader(multipartFile.getInputStream())) {
            BeanListProcessor<GameSaleDTO> rowProcessor = new BeanListProcessor<>(GameSaleDTO.class);
            CsvParserSettings settings = new CsvParserSettings();
            settings.setHeaderExtractionEnabled(true);
            settings.setProcessor(rowProcessor);
            CsvParser parser = new CsvParser(settings);
            parser.parse(inputReader);
            return rowProcessor.getBeans();
        } catch (IOException e) {
            throw e;
        }
    }

    public boolean isValidateDateFormat(String dateValue) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setLenient(false);

        Date parsedDate = null;

        try {
            parsedDate = formatter.parse(dateValue);
        } catch (ParseException e) {
            return false;
        }

        return true;
    }
}
