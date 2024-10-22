package com.demo.gamesales.upload;

import com.univocity.parsers.annotations.Convert;
import com.univocity.parsers.annotations.Parsed;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GameSaleDTO {

    public GameSaleDTO() {}

    @Parsed()
    private int id;

    @Parsed(field = "game_no")
    private int gameNo;

    @Parsed(field = "game_name")
    private String gameName;

    @Parsed(field = "game_code")
    private String gameCode;

    @Parsed()
    private int type;

    @Parsed(field = "cost_price")
    private double costPrice;

    @Parsed()
    private int percentage;

    @Parsed(field = "sale_price")
    private double salePrice;

    @Parsed(field = "sale_date")
    @Convert(conversionClass = LocalDateFormatter.class, args = "yyyy-MM-dd")
    private LocalDate saleDate;
}