package io.joshatron.bgt.server.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class RequestGame {
    private String playerIndicator;
    private String[] opponents;
    private Map<String,String> parameters;
}
