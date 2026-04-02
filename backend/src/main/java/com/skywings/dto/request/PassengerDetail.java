package com.skywings.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PassengerDetail {

    @NotBlank(message = "Passenger name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s.'-]+$", message = "Name can only contain letters, spaces, dots, hyphens, and apostrophes")
    private String name;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must be at most 120")
    private Integer age;

    @Size(max = 20, message = "Passport number must not exceed 20 characters")
    @Pattern(regexp = "^[A-Za-z0-9]*$", message = "Passport number must be alphanumeric")
    private String passportNumber;

    @Pattern(regexp = "^(Male|Female|Other)?$", message = "Gender must be Male, Female, or Other")
    private String gender;

    @Pattern(regexp = "^(\\d{4}-\\d{2}-\\d{2})?$", message = "Date of birth must be in YYYY-MM-DD format")
    private String dateOfBirth;

    @Size(max = 50, message = "Nationality must not exceed 50 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]*$", message = "Nationality can only contain letters and spaces")
    private String nationality;

    @Pattern(regexp = "^(NO_PREFERENCE|VEG|NON_VEG|VEGAN|JAIN)?$", message = "Invalid meal preference")
    private String mealPreference;

    @Pattern(regexp = "^(NONE|WHEELCHAIR|VISUAL_IMPAIRMENT|HEARING_IMPAIRMENT|ELDERLY_ASSISTANCE|UNACCOMPANIED_MINOR)?$",
             message = "Invalid special assistance type")
    private String specialAssistance;
}
