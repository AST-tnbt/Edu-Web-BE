package com.se347.courseservice.controllers;

import org.springframework.web.bind.annotation.*;

import com.se347.courseservice.services.CategoryService;
import com.se347.courseservice.dtos.CategoryRequestDto;
import com.se347.courseservice.dtos.CategoryResponseDto;

import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;

    CategoryController(CategoryService categoryService){
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody CategoryRequestDto request) {

        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @GetMapping("/{categoryName}")
    public ResponseEntity<CategoryResponseDto> getCategoryByName(@PathVariable String categoryName) {
        return ResponseEntity.ok(categoryService.getCategoryByName(categoryName));
    }
    
    @PutMapping("/{categoryName}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable String categoryName, @RequestBody CategoryRequestDto request) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryName, request));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/predefined")
    public ResponseEntity<List<CategoryResponseDto>> getPredefinedCategories() {
        return ResponseEntity.ok(categoryService.getPredefinedCategories());
    }

    @GetMapping("/custom")
    public ResponseEntity<List<CategoryResponseDto>> getCustomCategories() {
        return ResponseEntity.ok(categoryService.getCustomCategories());
    }
}
