package com.rithish.trading;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseTradingRecord;
import org.ta4j.core.TradingRecord;

import com.rithish.trading.report.PerformanceMetrics;
import com.rithish.trading.report.PerformanceMetricsCalculator;

/**
 * Unit tests for PerformanceMetricsCalculator.
 *
 * <p>Verifies performance calculations produced from
 * completed trading positions.</p>
 */
class PerformanceMetricsCalculatorTest {

    private PerformanceMetricsCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator =
                new PerformanceMetricsCalculator();
    }

    @Test
    void shouldRejectNullBarSeries() {

        TradingRecord tradingRecord =
                new BaseTradingRecord();

        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(
                        null,
                        tradingRecord
                )
        );
    }

    @Test
    void shouldRejectNullTradingRecord() {

        BarSeries series =
                createSeries();

        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(
                        series,
                        null
                )
        );
    }

    @Test
    void shouldReturnZeroMetricsWhenThereAreNoTrades() {

        BarSeries series =
                createSeries();

        TradingRecord tradingRecord =
                new BaseTradingRecord();

        PerformanceMetrics metrics =
                calculator.calculate(
                        series,
                        tradingRecord
                );

        assertEquals(
                0,
                metrics.getTotalTrades()
        );

        assertEquals(
                0,
                metrics.getWinningTrades()
        );

        assertEquals(
                0,
                metrics.getLosingTrades()
        );

        assertEquals(
                0.0,
                metrics.getWinRate()
        );

        assertEquals(
                0.0,
                metrics.getTotalProfit()
        );

        assertEquals(
                0.0,
                metrics.getTotalLoss()
        );

        assertEquals(
                0.0,
                metrics.getTotalEarnings()
        );

        assertEquals(
                0.0,
                metrics.getAverageProfit()
        );

        assertEquals(
                0.0,
                metrics.getMaximumDrawdown()
        );

        assertEquals(
                0.0,
                metrics.getProfitFactor()
        );
    }

    @Test
    void shouldCalculateWinningTradeMetrics() {

        BarSeries series =
                createSeries();

        BaseTradingRecord tradingRecord =
                new BaseTradingRecord();

        /*
         * Buy at 100 and sell at 110.
         * Profit = 10.
         */
        tradingRecord.enter(
                0,
                series.numFactory().numOf(100),
                series.numFactory().numOf(1)
        );

        tradingRecord.exit(
                1,
                series.numFactory().numOf(110),
                series.numFactory().numOf(1)
        );

        PerformanceMetrics metrics =
                calculator.calculate(
                        series,
                        tradingRecord
                );

        assertEquals(
                1,
                metrics.getTotalTrades()
        );

        assertEquals(
                1,
                metrics.getWinningTrades()
        );

        assertEquals(
                0,
                metrics.getLosingTrades()
        );

        assertEquals(
                1.0,
                metrics.getWinRate()
        );

        assertEquals(
                10.0,
                metrics.getTotalProfit(),
                0.000001
        );

        assertEquals(
                0.0,
                metrics.getTotalLoss(),
                0.000001
        );

        assertEquals(
                10.0,
                metrics.getTotalEarnings(),
                0.000001
        );

        assertEquals(
                10.0,
                metrics.getAverageProfit(),
                0.000001
        );

        assertTrue(
                Double.isInfinite(
                        metrics.getProfitFactor()
                )
        );
    }

    @Test
    void shouldCalculateLosingTradeMetrics() {

        BarSeries series =
                createSeries();

        BaseTradingRecord tradingRecord =
                new BaseTradingRecord();

        /*
         * Buy at 110 and sell at 100.
         * Loss = 10.
         */
        tradingRecord.enter(
                0,
                series.numFactory().numOf(110),
                series.numFactory().numOf(1)
        );

        tradingRecord.exit(
                1,
                series.numFactory().numOf(100),
                series.numFactory().numOf(1)
        );

        PerformanceMetrics metrics =
                calculator.calculate(
                        series,
                        tradingRecord
                );

        assertEquals(
                1,
                metrics.getTotalTrades()
        );

        assertEquals(
                0,
                metrics.getWinningTrades()
        );

        assertEquals(
                1,
                metrics.getLosingTrades()
        );

        assertEquals(
                0.0,
                metrics.getWinRate()
        );

        assertEquals(
                0.0,
                metrics.getTotalProfit(),
                0.000001
        );

        assertEquals(
                10.0,
                metrics.getTotalLoss(),
                0.000001
        );

        assertEquals(
                -10.0,
                metrics.getTotalEarnings(),
                0.000001
        );

        assertEquals(
                -10.0,
                metrics.getAverageProfit(),
                0.000001
        );

        assertEquals(
                0.0,
                metrics.getProfitFactor(),
                0.000001
        );
    }

    @Test
    void shouldCalculateMixedWinningAndLosingTrades() {

        BarSeries series =
                createSeries();

        BaseTradingRecord tradingRecord =
                new BaseTradingRecord();

        /*
         * Trade 1:
         * Buy 100 → Sell 120
         * Profit = 20
         */
        tradingRecord.enter(
                0,
                series.numFactory().numOf(100),
                series.numFactory().numOf(1)
        );

        tradingRecord.exit(
                1,
                series.numFactory().numOf(120),
                series.numFactory().numOf(1)
        );

        /*
         * Trade 2:
         * Buy 120 → Sell 110
         * Loss = 10
         */
        tradingRecord.enter(
                2,
                series.numFactory().numOf(120),
                series.numFactory().numOf(1)
        );

        tradingRecord.exit(
                3,
                series.numFactory().numOf(110),
                series.numFactory().numOf(1)
        );

        PerformanceMetrics metrics =
                calculator.calculate(
                        series,
                        tradingRecord
                );

        assertEquals(
                2,
                metrics.getTotalTrades()
        );

        assertEquals(
                1,
                metrics.getWinningTrades()
        );

        assertEquals(
                1,
                metrics.getLosingTrades()
        );

        /*
         * 1 winning trade / 2 total trades = 50%.
         */
        assertEquals(
                0.5,
                metrics.getWinRate(),
                0.000001
        );

        assertEquals(
                20.0,
                metrics.getTotalProfit(),
                0.000001
        );

        assertEquals(
                10.0,
                metrics.getTotalLoss(),
                0.000001
        );

        /*
         * Net earnings = 20 - 10 = 10.
         */
        assertEquals(
                10.0,
                metrics.getTotalEarnings(),
                0.000001
        );

        /*
         * Average = 10 / 2 = 5.
         */
        assertEquals(
                5.0,
                metrics.getAverageProfit(),
                0.000001
        );

        /*
         * Profit factor = 20 / 10 = 2.
         */
        assertEquals(
                2.0,
                metrics.getProfitFactor(),
                0.000001
        );
    }

    /**
     * Creates a small BarSeries used by the tests.
     *
     * <p>The actual prices used for the positions are supplied
     * directly through the TradingRecord, so the series only
     * needs to provide enough bars for the trading positions
     * and drawdown criterion.</p>
     */
   private BarSeries createSeries() {

    return new com.rithish.trading.loader.CsvMarketDataLoader()
            .loadSeries("TCS");
}
}