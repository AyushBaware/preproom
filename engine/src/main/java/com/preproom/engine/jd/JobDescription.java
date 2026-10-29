package com.preproom.engine.jd;

import java.util.List;
import java.util.Map;

/** Structured JD. A null field or an empty list means "not stated". Nothing is guessed. */
public record JobDescription(
        String company,
        String roleTitle,
        String employmentType,
        String location,
        String workMode,
        List<Item> requiredSkills,
        List<Item> preferredSkills,
        List<Item> responsibilities,
        List<Item> softTraits,
        String applicationProcess,
        Logistics logistics,
        Map<String, String> otherDetails) {}