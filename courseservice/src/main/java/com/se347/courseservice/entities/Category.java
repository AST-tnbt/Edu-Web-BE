package com.se347.courseservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {
    @Id
    @Column(nullable = false, length = 100, unique = true)
    private String categoryName;

    @Column(length = 100)
    private String description;
}
