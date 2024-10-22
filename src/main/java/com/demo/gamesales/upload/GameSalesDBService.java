package com.demo.gamesales.upload;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
@Slf4j
@Service
public class GameSalesDBService {
    @Autowired
    HikariDataSource hikariDataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    static class GameSaleRowMapper implements RowMapper<GameSaleDTO> {

        public GameSaleDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            GameSaleDTO gameSaleDTO = new GameSaleDTO();
            gameSaleDTO.setId(rs.getInt("ID"));
            gameSaleDTO.setGameNo(rs.getInt("GAME_NO"));
            gameSaleDTO.setGameName(rs.getString("GAME_NAME"));
            gameSaleDTO.setGameCode(rs.getString("GAME_CODE"));
            gameSaleDTO.setType(rs.getInt("TYPE"));
            gameSaleDTO.setCostPrice(rs.getDouble("COST_PRICE"));
            gameSaleDTO.setPercentage(rs.getInt("PERCENTAGE"));
            gameSaleDTO.setSalePrice(rs.getDouble("SALE_PRICE"));
            gameSaleDTO.setSaleDate(LocalDate.parse(rs.getDate("SALE_DATE").toString()));
            return gameSaleDTO;
        }
    }

    public void saveRecordsToDB(List<String[]> gameSalesList, String fileName) {

        int uploadHistoryId = insertUploadHistory(fileName, gameSalesList.size());

        int recordsInserted = saveAllInParallel(gameSalesList,uploadHistoryId);

        updateUploadHistory(recordsInserted,uploadHistoryId);

        checkTables();
    }

    private int insertUploadHistory(String fileName, int fileCount) {

        String sql = "INSERT INTO GAME_SALES_UPLOAD_HISTORY (FILE_NAME, FILE_RECORDS_COUNT) VALUES (:fileName,:fileRecordsCount)";

        MapSqlParameterSource namedParameters = new MapSqlParameterSource();

        namedParameters.addValue("fileName", fileName, Types.VARCHAR);

        namedParameters.addValue("fileRecordsCount", fileCount, Types.INTEGER);

        KeyHolder keyHolder = new GeneratedKeyHolder();

        namedParameterJdbcTemplate.update(sql, namedParameters, keyHolder);

        return keyHolder.getKey().intValue();

    }

    private int saveAllInParallel(List<String[]> gameSalesList, int uploadHistoryId) {

        List<List<String[]>> splitIntoMultipleList = splitIntoMultipleList(gameSalesList, 10000);

        ExecutorService executorService = Executors.newFixedThreadPool(hikariDataSource.getMaximumPoolSize());

        List<Callable<Integer>> callables = splitIntoMultipleList.stream().map(sublist ->
                (Callable<Integer>) () -> {
                    saveAll(sublist,uploadHistoryId);
                    return sublist.size();
                }).collect(Collectors.toList());

        int count = 0;

        try {
            List<Future<Integer>> futures = executorService.invokeAll(callables);

            for (Future<Integer> future : futures) {
                count += future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("saveAllInParallel error: ",e);
        }

        return count;
    }

    private void saveAll(List<String[]> gameSalesList, int uploadHistoryId) {
        String sql = "INSERT INTO GAME_SALES (ID, GAME_NO, GAME_NAME, GAME_CODE, TYPE, COST_PRICE, PERCENTAGE, SALE_PRICE, SALE_DATE, UPLOAD_HISTORY_ID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String errorArrayDump = "";

        try (Connection connection = hikariDataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
        ) {
            int counter = 0;
            for (String[] strings : gameSalesList) {
                errorArrayDump = Arrays.toString(strings);
                ps.clearParameters();
                ps.setInt(1, Integer.parseInt(strings[0]));
                ps.setInt(2, Integer.parseInt(strings[1]));
                ps.setString(3, strings[2]);
                ps.setString(4, strings[3]);
                ps.setInt(5, Integer.parseInt(strings[4]));
                ps.setDouble(6, Double.parseDouble(strings[5]));
                ps.setInt(7, Integer.parseInt(strings[6]));
                ps.setDouble(8, Double.parseDouble(strings[7]));
                ps.setDate(9, Date.valueOf(strings[8]));
                ps.setInt(10, uploadHistoryId);
                ps.addBatch();
                if ((counter + 1) % 2000 == 0 || (counter + 1) == gameSalesList.size()) {
                    ps.executeBatch();
                    ps.clearBatch();
                }
                counter++;
            }
        } catch (Exception e) {
            log.error("Error with row: " + errorArrayDump, e);
        }
    }

    private List<List<String[]>> splitIntoMultipleList(List<String[]> gameSalesList, int splitSize) {
        List<List<String[]>> listOfSplitList = new ArrayList<>();
        for (int i = 0; i < gameSalesList.size(); i += splitSize) {
            if (i + splitSize <= gameSalesList.size()) {
                listOfSplitList.add(gameSalesList.subList(i, i + splitSize));
            } else {
                listOfSplitList.add(gameSalesList.subList(i, gameSalesList.size()));
            }
        }
        return listOfSplitList;
    }

    private void updateUploadHistory(int insertedRecordCount, int uploadId) {

        String sql = "UPDATE GAME_SALES_UPLOAD_HISTORY SET UPLOADED_RECORDS_COUNT = :count, UPLOAD_END_TIME = CURRENT_TIMESTAMP WHERE ID=:id";

        MapSqlParameterSource namedParameters = new MapSqlParameterSource();

        namedParameters.addValue("count", insertedRecordCount, Types.INTEGER);

        namedParameters.addValue("id", uploadId, Types.INTEGER);

        namedParameterJdbcTemplate.update(sql, namedParameters);

    }

    private void checkTables() {

        String sql = "CHECK TABLE GAME_SALES, DAILY_SALES_VOL, DAILY_REVENUE";

        jdbcTemplate.query(sql, rs -> {
            int count = 1;
            do {
                String result = rs.getString(1);
                log.info("Table check " + count + " : " + result);
                count++;
            } while (rs.next());
        });
    }

    public List<GameSaleDTO> getGameSales(Map<String, Object> filters) {
        StringBuilder stringBuilder = new StringBuilder("SELECT * FROM GAME_SALES");
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();

        int pageRequested = (Integer) filters.get("page");

        if (pageRequested == 1) {
            stringBuilder.append(" WHERE ID > 0");
        } else {
            stringBuilder.append(" WHERE ID > ").append(pageRequested * 100);
        }

        for (String filter : filters.keySet()) {

            if (filter.equals("page")) {
                continue;
            }

            stringBuilder.append(" AND ");

            switch (filter) {
                case "fromDate":
                    stringBuilder.append("SALE_DATE >= :fromDate");
                    namedParameters.addValue("fromDate", Date.valueOf((String) filters.get(filter)), Types.DATE);
                    break;
                case "toDate":
                    stringBuilder.append("SALE_DATE <= :toDate");
                    namedParameters.addValue("toDate", Date.valueOf((String) filters.get(filter)), Types.DATE);
                    break;
                case "priceLessThan":
                    stringBuilder.append("SALE_PRICE < :priceLessThan");
                    namedParameters.addValue("priceLessThan", filters.get(filter), Types.INTEGER);
                    break;
                case "priceMoreThan":
                    stringBuilder.append("SALE_PRICE > :priceMoreThan");
                    namedParameters.addValue("priceMoreThan", filters.get(filter), Types.INTEGER);
                    break;
            }

        }

        stringBuilder.append(" ORDER BY ID LIMIT 100");

        String sql = stringBuilder.toString();

        log.info("getGameSales sql: " + sql);

        return namedParameterJdbcTemplate.query(sql, namedParameters, new GameSaleRowMapper());
    }

    public Integer getTotalSales(Map<String,Object> filters) {
        StringBuilder stringBuilder = new StringBuilder();
        MapSqlParameterSource namedParameters = new MapSqlParameterSource();

        switch ((String)filters.get("category")) {
            case "volume":
                stringBuilder.append("SELECT SUM(SALES_COUNT) FROM DAILY_SALES_VOL");
                break;
            case "sales":
                stringBuilder.append("SELECT SUM(REVENUE) FROM DAILY_REVENUE");
                break;
        }

        namedParameters.addValue("fromDate", Date.valueOf((String)filters.get("fromDate")), Types.DATE);

        if (filters.containsKey("toDate")) {
            stringBuilder.append(" WHERE SALE_DATE >= :fromDate AND SALE_DATE <= :toDate");
            namedParameters.addValue("toDate", Date.valueOf((String)filters.get("toDate")), Types.DATE);
        }
        else {
            stringBuilder.append(" WHERE SALE_DATE = :fromDate");

        }

        if (filters.containsKey("gameNo")) {
            stringBuilder.append(" AND GAME_NO = :gameNo");
            namedParameters.addValue("gameNo", filters.get("gameNo"), Types.INTEGER);
        }

        String sql = stringBuilder.toString();

        log.info("getTotalSales sql: " + sql);

        return namedParameterJdbcTemplate.queryForObject(sql,namedParameters, Integer.class);
    }
}
