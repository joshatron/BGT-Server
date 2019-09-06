package io.joshatron.bgt.server.database.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Config {
    @Id
    private String key;
    @Column(unique = true, nullable = false)
    private String value;
}
