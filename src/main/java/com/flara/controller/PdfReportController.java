package com.flara.controller;

import com.flara.service.PdfReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Serves the generated PDF health report, under {@code /api/report}.
 *
 * <p>The only controller in the application that returns something other than
 * JSON, which is why it sets headers by hand rather than letting Spring
 * serialize a response object.
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class PdfReportController {

    private final PdfReportService pdfReportService;

    /**
     * Builds and downloads a report covering the last N days.
     * {@code GET /api/report/{days}}
     *
     * <p>Two headers do the work. The content type tells the browser this is a
     * PDF rather than text, and {@code Content-Disposition: attachment} makes it
     * download with a sensible filename instead of rendering inline.
     *
     * <p>The try/catch predates {@code GlobalExceptionHandler} and is now
     * redundant: an uncaught exception would reach the global handler and
     * produce a 500 anyway. Removing it would make this controller consistent
     * with the other six.
     *
     * @param userDetails the authenticated caller, whose data the report covers
     * @param days        how many days back from today to include
     * @return the PDF bytes as a download, or 500 if generation fails
     */
    @GetMapping("/{days}")
    public ResponseEntity<byte[]> generateReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable int days) {

        try {
            byte[] pdf = pdfReportService.generateReport(userDetails.getUsername(), days);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    "flara-health-report-" + days + "days.pdf");

            return ResponseEntity.ok().headers(headers).body(pdf);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}