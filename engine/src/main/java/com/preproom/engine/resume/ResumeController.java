package com.preproom.engine.resume;

import com.preproom.engine.document.PdfTextExtractor;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.preproom.engine.document.AiExtractionException;

@RestController
public class ResumeController {

    private final ResumeExtractionService service;
    private final boolean enabled;

    public ResumeController(ResumeExtractionService service,
                            @Value("${preproom.llmtest.enabled:false}") boolean enabled) {
        this.service = service;
        this.enabled = enabled;
    }

    @PostMapping("/api/resume/extract-pdf")
    public ResumeExtractionResult extractPdf(@RequestParam("file") MultipartFile file) throws IOException {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        try {
            return service.extract(PdfTextExtractor.extract(file.getBytes()));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (AiExtractionException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }
}