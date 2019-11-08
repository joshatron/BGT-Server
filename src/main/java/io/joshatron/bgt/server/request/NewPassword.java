package io.joshatron.bgt.server.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NewPassword {
    private String newPassword;
}
