package com.demo.gamesales.upload;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Slf4j
@Controller
@RequestMapping(path="/demo/game-sales")
public class GameSalesController {

    @Autowired
    private GameSalesService gameSalesService;

    @Autowired
    private GameSalesDBService gameSalesDBService;

    @PostMapping("/import")
    public ResponseEntity<String> handleFileUpload(@RequestParam(name="file",required=true) MultipartFile file) {

        String message;

        String uploadedFileName = file.getOriginalFilename();

        if (gameSalesService.hasCsvFormat(file)) {
            try {

                List<String[]> gameSaleDTOList = gameSalesService.extractRecordsFromCSV(file);

                if (gameSaleDTOList.size()==0) {
                    message = "File have no records: " + uploadedFileName ;
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                }

                if (!gameSalesService.isValidCSVHeaders(gameSaleDTOList.get(0))) {
                    message = "File have invalid headers: " + uploadedFileName;
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                }

                gameSaleDTOList.remove(0);

                gameSalesDBService.saveRecordsToDB(gameSaleDTOList, uploadedFileName);

                message = "File is uploaded successfully: " + uploadedFileName;

                return ResponseEntity.status(HttpStatus.OK).body(message);

            } catch (Exception e) {
                message = "File is not uploaded successfully: " + uploadedFileName + "!";

                log.error(message,e);
            }
        }

        message = "Please upload an csv file!";

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @PostMapping("/v2/import")
    public ResponseEntity<String> handleFileImport(@RequestParam(name="file",required=true) MultipartFile file) {

        String message;

        String uploadedFileName = file.getOriginalFilename();

        if (gameSalesService.hasCsvFormat(file)) {
            try {

                List<String[]> gameSaleDTOList = gameSalesService.extractRecordsFromCSV(file);

                if (gameSaleDTOList.size()==0) {
                    message = "File have no records: " + uploadedFileName ;
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                }

                if (!gameSalesService.isValidCSVHeaders(gameSaleDTOList.get(0))) {
                    message = "File have invalid headers: " + uploadedFileName;
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
                }

                gameSaleDTOList.remove(0);

                gameSalesDBService.importFileToDB(gameSaleDTOList, uploadedFileName);

                message = "File is uploaded successfully: " + uploadedFileName;

                return ResponseEntity.status(HttpStatus.OK).body(message);

            } catch (Exception e) {
                message = "File is not uploaded successfully: " + uploadedFileName + "!";

                log.error(message,e);
            }
        }

        message = "Please upload an csv file!";

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    @GetMapping(path="/getGameSales")
    public @ResponseBody List getGameSales(@RequestParam(required=true) int page, @RequestParam Optional<String> fromDate, @RequestParam Optional<String> toDate, @RequestParam  Optional<Integer> priceLessThan, @RequestParam  Optional<Integer> priceMoreThan) {

        Map<String,Object> paramsMap = new HashMap<>();

        if (page > 0) {
            paramsMap.put("page", page);
        }
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page value provided");
        }

        if (fromDate.isPresent()) {
            if (gameSalesService.isValidateDateFormat(fromDate.get())) {
                paramsMap.put("fromDate", fromDate.get());
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid from date value provided");
            }
        }

        if (toDate.isPresent()) {
            if (gameSalesService.isValidateDateFormat(toDate.get())) {
                paramsMap.put("toDate", toDate.get());
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid to date value provided");
            }
        }

        if (priceLessThan.isPresent()) {
            if (priceLessThan.get() > 0) {
                paramsMap.put("priceLessThan", priceLessThan.get());
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid price less than value provided");
            }
        }

        if (priceMoreThan.isPresent()) {
            if (priceMoreThan.get() > 0) {
                paramsMap.put("priceLessThan", priceMoreThan.get());
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid price more than value provided");
            }
        }

        return gameSalesDBService.getGameSales(paramsMap);
    }

    @GetMapping(path="/getTotalSales")
    public @ResponseBody Integer getTotalSales(@RequestParam(required=true) String category, @RequestParam(required=true) String fromDate, @RequestParam Optional<String> toDate,@RequestParam Optional<Integer> gameNo) {

        Map<String,Object> paramsMap = new HashMap<>();

        if (category.equalsIgnoreCase("volume") || category.equalsIgnoreCase("sales")) {
            paramsMap.put("category", category.toLowerCase());
        }
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid category value provided");
        }

        if (gameSalesService.isValidateDateFormat(fromDate)) {
            paramsMap.put("fromDate", fromDate);
        }
        else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid from date value provided");
        }

        if (toDate.isPresent()) {
            if (gameSalesService.isValidateDateFormat(toDate.get())) {
                paramsMap.put("toDate", toDate.get());
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid to date value provided");
            }
        }

        if (gameNo.isPresent()) {
            if (gameNo.get() > 0) {
                paramsMap.put("gameNo", gameNo.get());
            }
            else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid game number value provided");
            }

        }

        return gameSalesDBService.getTotalSales(paramsMap);
    }

}
