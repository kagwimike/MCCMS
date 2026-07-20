package com.example.mccms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "STAGES")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;
}
