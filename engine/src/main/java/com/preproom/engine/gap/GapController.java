package com.preproom.engine.gap;

import com.preproom.engine.document.AiExtractionException;
import com.preproom.engine.document.PdfTextExtractor;
import com.preproom.engine.jd.ExtractionResult;
import com.preproom.engine.jd.JdExtractionService;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class GapController {

    public record GapResponse(String roleTitle, GapReport report, List<String> jdRejected) {}

    private final JdExtractionService jdService;
    private final GapService gapService;
    private final boolean enabled;

    public GapController(JdExtractionService jdService, GapService gapService,
                         @Value("${preproom.llmtest.enabled:false}") boolean enabled) {
        this.jdService = jdService;
        this.gapService = gapService;
        this.enabled = enabled;
    }

    @PostMapping("/api/gap/analyze-pdf")
    public GapResponse analyze(@RequestParam("jd") MultipartFile jdFile,
                               @RequestParam("resume") MultipartFile resumeFile) throws IOException {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        try {
            String jdText = PdfTextExtractor.extract(jdFile.getBytes());
            String resumeText = PdfTextExtractor.extract(resumeFile.getBytes());
            ExtractionResult jd = jdService.extract(jdText);
            GapReport report = gapService.analyze(jd.jd(), resumeText);
            return new GapResponse(jd.jd().roleTitle(), report, jd.rejected());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (AiExtractionException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }
}