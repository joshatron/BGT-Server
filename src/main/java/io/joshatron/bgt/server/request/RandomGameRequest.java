package io.joshatron.bgt.server.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class RandomGameRequest {
    private Map<String,String> parameters;
}
