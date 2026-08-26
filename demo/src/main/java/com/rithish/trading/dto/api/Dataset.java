package com.rithish.trading.dto.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Dataset {

    private String symbol;
    private String exchange;
    private String interval;
}