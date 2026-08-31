package com.rithish.trading.controller;

import com.rithish.trading.dto.api.BacktestRequest;
import com.rithish.trading.dto.api.BacktestResponse;
import com.rithish.trading.dto.api.WalkForwardBacktestResponse;
import com.rithish.trading.service.impl.BacktestApiService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backtest")
@RequiredArgsConstructor
public class BacktestController {

    private final BacktestApiService backtestApiService;

    @PostMapping
    public ResponseEntity<BacktestResponse> runBacktest(
            @RequestBody BacktestRequest request) {

        BacktestResponse response =
                backtestApiService.execute(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/walk-forward")
    public ResponseEntity<WalkForwardBacktestResponse> runWalkForwardBacktest(
            @RequestBody BacktestRequest request) {

        WalkForwardBacktestResponse response =
                backtestApiService.executeWalkForward(request);

        return ResponseEntity.ok(response);
    }
}