package com.se347.courseservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.se347.courseservice.entities.Category;

public interface CategoryRepository extends JpaRepository<Category, String> {
    boolean existsByCategoryName(String categoryName);
}
