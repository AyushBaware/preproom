package com.preproom.engine.interview;

import com.preproom.engine.document.AiExtractionException;
import com.preproom.engine.document.PdfTextExtractor;
import com.preproom.engine.gap.GapReport;
import com.preproom.engine.gap.GapService;
import com.preproom.engine.jd.ExtractionResult;
import com.preproom.engine.jd.JdExtractionService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class KitController {

    public record KitResponse(String roleTitle, GapReport gap, InterviewKit kit) {}

    private final JdExtractionService jdService;
    private final GapService gapService;
    private final KitService kitService;
    private final boolean enabled;

    public KitController(JdExtractionService jdService, GapService gapService, KitService kitService,
                         @Value("${preproom.llmtest.enabled:false}") boolean enabled) {
        this.jdService = jdService;
        this.gapService = gapService;
        this.kitService = kitService;
        this.enabled = enabled;
    }

    @PostMapping("/api/interview/kit-pdf")
    public KitResponse kit(@RequestParam("jd") MultipartFile jdFile,
                           @RequestParam("resume") MultipartFile resumeFile) throws IOException {
        if (!enabled) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        try {
            String jdText = PdfTextExtractor.extract(jdFile.getBytes());
            String resumeText = PdfTextExtractor.extract(resumeFile.getBytes());
            ExtractionResult jd = jdService.extract(jdText);
            GapReport gap = gapService.analyze(jd.jd(), resumeText);
            InterviewKit kit = kitService.generate(jd.jd(), gap, jdText);
            return new KitResponse(jd.jd().roleTitle(), gap, kit);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (AiExtractionException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, e.getMessage());
        }
    }
}