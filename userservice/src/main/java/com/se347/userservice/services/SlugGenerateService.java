package com.se347.userservice.services;

import com.se347.userservice.repositories.UserProfileRepository;
import com.se347.userservice.utils.SlugUtil;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Random;

@Service
@RequiredArgsConstructor
public class SlugGenerateService {

    private final UserProfileRepository userProfileRepository;
    private final Random random = new Random();

    /**
     * Generate random alphabetic string
     * @param length length of random string
     * @return random string containing only letters (a-z, A-Z)
     */
    private String generateRandomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public String generateSlug(String input) {
        String baseSlug = SlugUtil.toSlug(input);
        String candidateSlug = baseSlug;
        
        while (userProfileRepository.existsByUserSlug(candidateSlug)) {
            candidateSlug = baseSlug + "-" + generateRandomString(4);
        }
        
        return candidateSlug;
    }
}