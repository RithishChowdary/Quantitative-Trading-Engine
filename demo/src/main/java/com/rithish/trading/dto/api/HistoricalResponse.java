package com.rithish.trading.dto.api;

import com.rithish.trading.dto.historical.HistoricalCandle;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class HistoricalResponse {

    private List<Dataset> datasets;

    private List<HistoricalCandle> candles;

    public HistoricalResponse(
            List<Dataset> datasets,
            List<HistoricalCandle> candles) {

        this.datasets = datasets;
        this.candles = candles;
    }

    /*
     * Kept for compatibility with existing code/tests
     * that only provide dataset metadata.
     */
    public HistoricalResponse(List<Dataset> datasets) {
        this.datasets = datasets;
        this.candles = List.of();
    }
}