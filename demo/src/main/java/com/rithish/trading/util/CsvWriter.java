package com.rithish.trading.util;

import com.rithish.trading.dto.historical.HistoricalCandle;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** Writes normalized historical candles to CSV. */
public class CsvWriter {

    public void write(
            Path filePath,
            List<HistoricalCandle> candles)
            throws IOException {

        Objects.requireNonNull(filePath, "File path must not be null");
        Objects.requireNonNull(candles, "Candles must not be null");

        Path parent = filePath.toAbsolutePath().getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (BufferedWriter writer =
                     Files.newBufferedWriter(
                             filePath,
                             StandardCharsets.UTF_8)) {

            writer.write("Date,Open,High,Low,Close,Volume");
            writer.newLine();

            for (HistoricalCandle candle : candles) {
                Objects.requireNonNull(candle, "Candle must not be null");

                writer.write(
                        candle.getTimestamp().toLocalDate() + ","
                                + candle.getOpen() + ","
                                + candle.getHigh() + ","
                                + candle.getLow() + ","
                                + candle.getClose() + ","
                                + candle.getVolume()
                );
                writer.newLine();
            }
        }
    }
}
