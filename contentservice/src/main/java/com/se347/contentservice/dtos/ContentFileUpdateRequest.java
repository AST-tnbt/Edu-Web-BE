package com.se347.contentservice.dtos;

import com.se347.contentservice.enums.FileStatus;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentFileUpdateRequest {
    private String fileName;
    private FileStatus status;
}
