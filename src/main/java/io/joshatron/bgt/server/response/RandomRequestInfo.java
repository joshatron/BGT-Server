package io.joshatron.bgt.server.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RandomRequestInfo {
    private String requester;
    private int size;
}
