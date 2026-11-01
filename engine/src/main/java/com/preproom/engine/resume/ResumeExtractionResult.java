package com.preproom.engine.resume;

import java.util.List;

public record ResumeExtractionResult(Resume resume, List<String> rejected) {}