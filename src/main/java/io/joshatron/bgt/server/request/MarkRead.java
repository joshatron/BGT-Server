package io.joshatron.bgt.server.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class MarkRead {
    private String[] ids;
    private String[] senders;
}
