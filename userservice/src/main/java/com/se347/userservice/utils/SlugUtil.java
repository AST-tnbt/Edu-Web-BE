package com.se347.userservice.utils;

import java.text.Normalizer;

public class SlugUtil {

    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // 1. Chuẩn hóa, tách dấu tiếng Việt
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String noAccent = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 2. Chuyển về lowercase
        String lower = noAccent.toLowerCase();

        // 3. Thay ký tự không phải chữ/số thành dấu '-'
        String slug = lower.replaceAll("[^a-z0-9]+", "-");

        // 4. Xóa dấu '-' dư
        slug = slug.replaceAll("-{2,}", "-");

        // 5. Xóa '-' ở đầu/cuối
        slug = slug.replaceAll("^-|-$", "");

        return slug;
    }

}

