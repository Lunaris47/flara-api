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
 * Generates and returns a PDF health report for doctor appointments.
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class PdfReportController {

    private final PdfReportService pdfReportService;

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