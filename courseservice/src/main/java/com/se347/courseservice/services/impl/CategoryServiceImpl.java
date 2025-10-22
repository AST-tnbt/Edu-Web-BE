package com.se347.courseservice.services.impl;

import com.se347.courseservice.dtos.CategoryRequestDto;
import com.se347.courseservice.dtos.CategoryResponseDto;
import com.se347.courseservice.entities.Category;
import com.se347.courseservice.enums.CourseCategory;
import com.se347.courseservice.exceptions.CourseException;
import com.se347.courseservice.repositories.CategoryRepository;
import com.se347.courseservice.services.CategoryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryResponseDto createCategory(CategoryRequestDto request) {
        if (request == null || request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Category name cannot be null or empty");
        }

        String categoryName = request.getCategoryName().trim();
        
        // Check if it's a predefined category
        if (CourseCategory.isValidCategory(categoryName)) {
            throw new CourseException.CategoryAlreadyExistsException("Category '" + categoryName + "' is a predefined category");
        }

        // Check if custom category already exists
        if (categoryRepository.existsById(categoryName)) {
            throw new CourseException.CategoryAlreadyExistsException("Category '" + categoryName + "' already exists");
        }

        Category category = Category.builder()
                .categoryName(categoryName)
                .build();

        categoryRepository.save(category);
        return mapToResponse(category);
    }

    @Override
    public CategoryResponseDto getCategoryByName(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Category name cannot be null or empty");
        }

        // First check predefined categories
        CourseCategory predefinedCategory = CourseCategory.fromDisplayName(categoryName);
        if (predefinedCategory != null) {
            return CategoryResponseDto.builder()
                    .categoryName(predefinedCategory.getDisplayName())
                    .description(predefinedCategory.getDescription())
                    .isPredefined(true)
                    .build();
        }

        // Then check custom categories
        Category customCategory = categoryRepository.findById(categoryName)
                .orElseThrow(() -> new CourseException.CategoryNotFoundException(categoryName));

        return mapToResponse(customCategory);
    }

    @Override
    public CategoryResponseDto updateCategory(String categoryName, CategoryRequestDto request) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            throw new CourseException.InvalidRequestException("Category name cannot be null or empty");
        }

        // Cannot update predefined categories
        if (CourseCategory.isValidCategory(categoryName)) {
            throw new CourseException.InvalidRequestException("Cannot update predefined category: " + categoryName);
        }

        Category existingCategory = categoryRepository.findById(categoryName)
                .orElseThrow(() -> new CourseException.CategoryNotFoundException(categoryName));

        // Update category if new name is provided
        if (request.getCategoryName() != null && !request.getCategoryName().trim().isEmpty()) {
            String newCategoryName = request.getCategoryName().trim();
            
            // Check if new name conflicts with predefined categories
            if (CourseCategory.isValidCategory(newCategoryName)) {
                throw new CourseException.InvalidRequestException("Cannot rename to predefined category: " + newCategoryName);
            }

            // Check if new name already exists
            if (!newCategoryName.equals(categoryName) && categoryRepository.existsById(newCategoryName)) {
                throw new CourseException.CategoryAlreadyExistsException("Category '" + newCategoryName + "' already exists");
            }

            existingCategory.setCategoryName(newCategoryName);
        }

        categoryRepository.save(existingCategory);
        return mapToResponse(existingCategory);
    }

    // @Override
    // public void deleteCategory(String categoryName) {
    //     if (categoryName == null || categoryName.trim().isEmpty()) {
    //         throw new CourseException.InvalidRequestException("Category name cannot be null or empty");
    //     }

    //     // Cannot delete predefined categories
    //     if (CourseCategory.isValidCategory(categoryName)) {
    //         throw new CourseException.InvalidRequestException("Cannot delete predefined category: " + categoryName);
    //     }

    //     if (!categoryRepository.existsById(categoryName)) {
    //         throw new CourseException.CategoryNotFoundException(categoryName);
    //     }

    //     categoryRepository.deleteById(categoryName);
    // }

    @Override
    public List<CategoryResponseDto> getAllCategories() {
        // Combine predefined and custom categories
        List<CategoryResponseDto> predefinedCategories = getPredefinedCategories();
        List<CategoryResponseDto> customCategories = getCustomCategories();
        
        return Stream.concat(predefinedCategories.stream(), customCategories.stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDto> searchCategoriesByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllCategories();
        }

        String searchTerm = name.trim().toLowerCase();
        
        // Search in predefined categories
        List<CategoryResponseDto> predefinedMatches = CourseCategory.getAllDisplayNames().stream()
                .filter(displayName -> displayName.toLowerCase().contains(searchTerm))
                .map(displayName -> {
                    CourseCategory category = CourseCategory.fromDisplayName(displayName);
                    return CategoryResponseDto.builder()
                            .categoryName(category.getDisplayName())
                            .description(category.getDescription())
                            .isPredefined(true)
                            .build();
                })
                .collect(Collectors.toList());

        // Search in custom categories
        List<CategoryResponseDto> customMatches = categoryRepository.findAll().stream()
                .filter(category -> category.getCategoryName().toLowerCase().contains(searchTerm))
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return Stream.concat(predefinedMatches.stream(), customMatches.stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDto> getPredefinedCategories() {
        return CourseCategory.getAllDisplayNames().stream()
                .map(displayName -> {
                    CourseCategory category = CourseCategory.fromDisplayName(displayName);
                    return CategoryResponseDto.builder()
                            .categoryName(category.getDisplayName())
                            .description(category.getDescription())
                            .isPredefined(true)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponseDto> getCustomCategories() {
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPredefinedCategory(String categoryName) {
        return CourseCategory.isValidCategory(categoryName);
    }

    @Override
    public boolean isCustomCategory(String categoryName) {
        return categoryRepository.existsById(categoryName);
    }

    @Override
    public boolean categoryExists(String categoryName) {
        return isPredefinedCategory(categoryName) || isCustomCategory(categoryName);
    }

    @Override
    public boolean canCreateCategory(String categoryName) {
        return !isPredefinedCategory(categoryName) && !isCustomCategory(categoryName);
    }

    private CategoryResponseDto mapToResponse(Category category) {
        return CategoryResponseDto.builder()
                .categoryName(category.getCategoryName())
                .description(category.getDescription())
                .isPredefined(false)
                .build();
    }
}
