package com.eduweb.config;

import com.eduweb.exception.GlobalExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(GlobalExceptionHandler.class)
public class CoreExceptionConfiguration { }
