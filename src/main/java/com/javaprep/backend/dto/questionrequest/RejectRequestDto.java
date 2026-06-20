package com.javaprep.backend.dto.questionrequest;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectRequestDto {

    @NotBlank(message = "reviewNotes is required when rejecting a request")
    private String reviewNotes;
}
