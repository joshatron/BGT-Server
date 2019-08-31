package io.joshatron.bgt.server.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameRequest {
    private int size;
    private String requesterColor;
    private String first;
}
