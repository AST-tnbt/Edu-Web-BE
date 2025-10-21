package com.se347.courseservice.services;

import com.se347.courseservice.dtos.CategoryRequestDto;
import com.se347.courseservice.dtos.CategoryResponseDto;
import java.util.List;

public interface CategoryService {
    // Category management
    CategoryResponseDto createCategory(CategoryRequestDto request);
    CategoryResponseDto getCategoryByName(String categoryName);
    CategoryResponseDto updateCategory(String categoryName, CategoryRequestDto request);
    void deleteCategory(String categoryName);
    List<CategoryResponseDto> getAllCategories();
    List<CategoryResponseDto> searchCategoriesByName(String name);
    
    // Enum category operations
    List<CategoryResponseDto> getPredefinedCategories();
    List<CategoryResponseDto> getCustomCategories();
    boolean isPredefinedCategory(String categoryName);
    boolean isCustomCategory(String categoryName);
    
    // Category validation
    boolean categoryExists(String categoryName);
    boolean canCreateCategory(String categoryName);
}
