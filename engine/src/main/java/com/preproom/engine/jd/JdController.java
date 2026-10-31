package com.preproom.engine.jd;

import com.preproom.engine.document.PdfTextExtractor;
import java.io.IOException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class JdController {

    private final JdExtractionService service;
    private final boolean enabled;

    public JdController(JdExtractionService service,
                        @Value("${preproom.llmtest.enabled:false}") boolean enabled) {
        this.service = service;
        this.enabled = enabled;
    }

    @PostMapping("/api/jd/extract")
    public ExtractionResult extract(@RequestBody Map<String, String> body) {
        requireEnabled();
        return run(body.get("text"));
    }

    @PostMapping("/api/jd/extract-pdf")
    public ExtractionResult extractPdf(@RequestParam("file") MultipartFile file) throws IOException {
        requireEnabled();
        String text;
        try {
            text = PdfTextExtractor.extract(file.getBytes());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return run(text);
    }

    private ExtractionResult run(String text) {
        try {
            return service.extract(text);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private void requireEnabled() {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}