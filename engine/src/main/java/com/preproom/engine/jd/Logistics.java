package com.preproom.engine.jd;

/** Details that usually come from the placement email. A null field means "not stated". */
public record Logistics(String duration, String stipend, String ppo, String ctc, String deadline) {}