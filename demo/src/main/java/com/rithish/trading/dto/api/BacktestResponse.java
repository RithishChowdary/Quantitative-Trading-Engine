package com.rithish.trading.dto.api;

import com.rithish.trading.report.PerformanceMetrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResponse {

    private String symbol;

    private PerformanceMetrics performanceMetrics;
}