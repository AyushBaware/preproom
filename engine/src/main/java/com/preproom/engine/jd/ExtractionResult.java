package com.preproom.engine.jd;

import java.util.List;

public record ExtractionResult(JobDescription jd, List<String> rejected) {}