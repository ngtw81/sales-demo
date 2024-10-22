package com.demo.gamesales.upload;

import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.common.processor.BeanWriterProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;

import java.io.*;
import java.security.SecureRandom;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GameSalesDataBuilder {

    private final Random random = new Random();
    private int total;

    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();

    public static GameSalesDataBuilder builder() {
        return new GameSalesDataBuilder();
    }

    public GameSalesDataBuilder setTotal(int total) {
        this.total = total;
        return this;
    }

    public void build() {
        Date fromDate = Date.valueOf("2024-04-01");

        Date toDate = Date.valueOf("2024-05-01");

        List<GameSaleDTO> gameSaleDTOList = new ArrayList<>();

        for (int x = 1; x <= total; x++) {
            double costPrice = random.nextDouble(40,100);
            GameSaleDTO gameSaleDTO = new GameSaleDTO();

            gameSaleDTO.setId(x);
            gameSaleDTO.setGameNo(random.nextInt((100 + 1) - 1) + 1);
            gameSaleDTO.setGameName("GameName" + x);
            gameSaleDTO.setGameCode(generateRandomGameCode(5));
            gameSaleDTO.setType(random.nextInt(2));
            gameSaleDTO.setCostPrice(Math.round(costPrice * 100.0) / 100.0);
            gameSaleDTO.setPercentage(9);
            gameSaleDTO.setSalePrice(Math.round(costPrice*1.09f* 100.0) / 100.0);
            Date randomSaleDate = new Date(ThreadLocalRandom.current().nextLong(fromDate.getTime(),toDate.getTime()));
            gameSaleDTO.setSaleDate(LocalDate.parse(new SimpleDateFormat("yyyy-MM-dd").format(randomSaleDate)));
            gameSaleDTOList.add(gameSaleDTO);
        }

        try (Writer outputWriter = new OutputStreamWriter(new FileOutputStream(new File("game_sales.csv")), "UTF-8")) {
            BeanWriterProcessor<GameSaleDTO> rowProcessor = new BeanWriterProcessor<GameSaleDTO>(GameSaleDTO.class);
            CsvWriterSettings csvWriterSettings= new CsvWriterSettings();
            csvWriterSettings.setHeaders("id","game_no","game_name","game_code","type","cost_price","percentage","sale_price","sale_date");
            csvWriterSettings.setRowWriterProcessor(rowProcessor);
            CsvWriter writer = new CsvWriter(outputWriter, csvWriterSettings);
            writer.writeHeaders();
            for (GameSaleDTO gameSaleDTO : gameSaleDTOList) {
                writer.processRecord(gameSaleDTO);
            }
            writer.close();

        } catch (IOException e) {
            // handle exception
        }
    }

    private static String generateRandomGameCode(int len) {

        StringBuilder sb = new StringBuilder(len);

        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));

        return sb.toString();
    }

    private static void readRaw() {

        try (Reader inputReader = new InputStreamReader(new FileInputStream(new File("test.csv")), "UTF-8")) {
            long startTime = System.currentTimeMillis();
            CsvParser parser = new CsvParser(new CsvParserSettings());
            List<String[]> parsedRows = parser.parseAll(inputReader);
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println("Read raw duration:" + TimeUnit.MILLISECONDS.toSeconds(elapsedTime));
        } catch (IOException e) {
            // handle exception
        }
    }

    private static void readToBeans() {

        try (Reader inputReader = new InputStreamReader(new FileInputStream(new File("test.csv")), "UTF-8")) {
            long startTime = System.currentTimeMillis();
            BeanListProcessor<GameSaleDTO> rowProcessor = new BeanListProcessor<>(GameSaleDTO.class);
            CsvParserSettings settings = new CsvParserSettings();
            settings.setHeaderExtractionEnabled(true);
            settings.setProcessor(rowProcessor);
            CsvParser parser = new CsvParser(settings);
            parser.parse(inputReader);
            long stopTime = System.currentTimeMillis();
            long elapsedTime = stopTime - startTime;
            System.out.println("Read to beans duration:" + TimeUnit.MILLISECONDS.toSeconds(elapsedTime));
        } catch (IOException e) {
            // handle exception
        }
    }

    public static void main(String[] args) {
        GameSalesDataBuilder.builder().setTotal(1000000).build();
    }
}
