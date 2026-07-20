package com.example.mccms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ROLES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 20, unique = true)
    private String name;

    public static final String ADMIN = "ADMIN";
    public static final String CREATOR = "CREATOR";
    public static final String REVIEWER = "REVIEWER";
}
