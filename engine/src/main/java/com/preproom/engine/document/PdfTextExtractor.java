package com.preproom.engine.document;

import java.io.IOException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

/** Reads the text of a text-based PDF. Scanned or image PDFs return little or no text. */
public final class PdfTextExtractor {

    private static final int MAX_PAGES = 20;

    private PdfTextExtractor() {}

    public static String extract(byte[] pdfBytes) throws IOException {
        try (PDDocument document = Loader.loadPDF(pdfBytes)) {
            if (document.getNumberOfPages() > MAX_PAGES) {
                throw new IllegalArgumentException("PDF has more than " + MAX_PAGES + " pages");
            }
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document).trim();
        }
    }
}