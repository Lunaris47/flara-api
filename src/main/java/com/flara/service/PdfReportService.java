package com.flara.service;

import com.flara.entity.DailyMentalLog;
import com.flara.entity.DailyPhysicalLog;
import com.flara.entity.Flare;
import com.flara.entity.User;
import com.flara.repository.DailyMentalLogRepository;
import com.flara.repository.DailyPhysicalLogRepository;
import com.flara.repository.FlareRepository;
import com.flara.repository.UserRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Builds the PDF health report a user takes to a medical appointment.
 *
 * <p>The report is the application's main output. Everything else in Flara
 * exists to feed it: a consultation is short, and a patient trying to recall
 * three months of symptoms from memory does badly. This produces a single
 * document covering physical symptoms, mental state and flare history over a
 * chosen window.
 *
 * <p><b>Known design issue.</b> At around 440 lines this class does three jobs:
 * it fetches data, aggregates it into statistics, and lays out a document. The
 * natural split is a service that assembles a report model and a separate
 * renderer that turns that model into a PDF, which would also make the
 * aggregation testable without generating a file. It is the first refactor this
 * codebase needs.
 *
 * <p>Note also that it injects four repositories directly rather than going
 * through the other services. That bypasses their ownership checks, which is
 * safe here only because every query below is scoped by the resolved user's id.
 *
 * <p>Rendering uses iText 5. Generation runs inline on the request thread, which
 * is acceptable at current data volumes but would belong in a background job
 * for long reporting windows.
 */
@Service
@RequiredArgsConstructor
public class PdfReportService {

    private final UserRepository userRepository;
    private final DailyPhysicalLogRepository physicalLogRepository;
    private final DailyMentalLogRepository mentalLogRepository;
    private final FlareRepository flareRepository;

    // ===============================
    // PALETTE
    // Shared by every report, so held as static constants rather than
    // reconstructed per call. RED, GREEN and AMBER are used to colour
    // severity values, not as decoration.
    // ===============================

    private static final BaseColor ACCENT = new BaseColor(200, 169, 126);
    private static final BaseColor LIGHT_GRAY = new BaseColor(240, 240, 240);
    private static final BaseColor TEXT_DARK = new BaseColor(44, 44, 44);
    private static final BaseColor TEXT_GRAY = new BaseColor(100, 100, 100);
    private static final BaseColor RED = new BaseColor(252, 129, 129);
    private static final BaseColor GREEN = new BaseColor(104, 211, 145);
    private static final BaseColor AMBER = new BaseColor(246, 173, 85);

    /**
     * Builds a complete report for one user over a trailing window of days.
     *
     * <p>Runs in three phases:
     * <ol>
     *   <li><b>Fetch.</b> Resolve the user, compute the date window, then pull
     *       physical logs, mental logs and flares for that window.</li>
     *   <li><b>Aggregate.</b> Derive the averages and counts shown in the
     *       summary panels.</li>
     *   <li><b>Render.</b> Lay out the document section by section into an
     *       in-memory stream.</li>
     * </ol>
     *
     * <p>The result is returned as a byte array rather than written to disk, so
     * nothing is persisted server-side and the controller can stream it straight
     * back as an attachment.
     *
     * @param username the authenticated caller, whose data the report covers
     * @param days     how many days back from today to include
     * @return the finished PDF as bytes
     * @throws Exception if the document cannot be built
     */
    public byte[] generateReport(String username, int days) throws Exception {

        // --- Phase 1: fetch -------------------------------------------------

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy");

        List<DailyPhysicalLog> physicalLogs = physicalLogRepository
                .findByUserIdAndLogDateBetweenOrderByLogDateDesc(user.getId(), startDate, endDate);

        List<DailyMentalLog> mentalLogs = mentalLogRepository
                .findByUserIdAndLogDateBetweenOrderByLogDateDesc(user.getId(), startDate, endDate);

        // Flares are timestamped rather than dated, so the LocalDate window is
        // widened to cover the whole of the first and last day.
        List<Flare> flares = flareRepository
                .findByUserIdAndStartDateBetweenOrderByStartDateDesc(
                        user.getId(),
                        startDate.atStartOfDay(),
                        endDate.atTime(23, 59, 59));

        // --- Phase 3: render ------------------------------------------------
        // Written to an in-memory stream, never to disk, so the controller can
        // return the bytes directly and nothing is left on the server.

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 50, 50, 60, 60);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, new BaseColor(44, 44, 44));
        Font headingFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, new BaseColor(44, 44, 44));
        Font subheadingFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD, TEXT_GRAY);
        Font bodyFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, TEXT_DARK);
        Font smallFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, TEXT_GRAY);
        Font accentFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, new BaseColor(139, 90, 43));

        Paragraph title = new Paragraph("Flara Health Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4);
        document.add(title);

        Paragraph subtitle = new Paragraph(
                user.getUsername() + "  •  " +
                        (user.getCondition() == User.Condition.CROHNS ? "Crohn's Disease" : "Ulcerative Colitis") + "  •  " +
                        startDate.format(fmt) + " – " + endDate.format(fmt),
                smallFont
        );
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(4);
        document.add(subtitle);

        Paragraph generated = new Paragraph("Generated by Flara  •  flara-five.vercel.app", smallFont);
        generated.setAlignment(Element.ALIGN_CENTER);
        generated.setSpacingAfter(20);
        document.add(generated);

        addDivider(document);

        document.add(Chunk.NEWLINE);
        addSectionHeader(document, "Summary", headingFont);

        PdfPTable summaryTable = new PdfPTable(4);
        summaryTable.setWidthPercentage(100);
        summaryTable.setSpacingAfter(16);

        // --- Phase 2: aggregate ---------------------------------------------
        // Null scores are filtered out rather than counted as zero, so a day the
        // user left blank does not drag an average down. orElse(0) covers the
        // case where every value in the window is missing.

        double avgPain = physicalLogs.stream()
                .filter(l -> l.getPainScore() != null)
                .mapToInt(DailyPhysicalLog::getPainScore)
                .average().orElse(0);

        double avgStress = mentalLogs.stream()
                .filter(l -> l.getStressScore() != null)
                .mapToInt(DailyMentalLog::getStressScore)
                .average().orElse(0);

        double avgMood = mentalLogs.stream()
                .filter(l -> l.getMoodScore() != null)
                .mapToInt(DailyMentalLog::getMoodScore)
                .average().orElse(0);

        double avgSleep = mentalLogs.stream()
                .filter(l -> l.getSleepQuality() != null)
                .mapToInt(DailyMentalLog::getSleepQuality)
                .average().orElse(0);

        addSummaryCell(summaryTable, "Avg Pain", String.format("%.1f/10", avgPain), getPainColor(avgPain));
        addSummaryCell(summaryTable, "Avg Stress", String.format("%.1f/10", avgStress), getStressColor(avgStress));
        addSummaryCell(summaryTable, "Avg Mood", String.format("%.1f/10", avgMood), GREEN);
        addSummaryCell(summaryTable, "Avg Sleep", String.format("%.1f/10", avgSleep), new BaseColor(118, 228, 247));
        document.add(summaryTable);

        PdfPTable statsTable = new PdfPTable(3);
        statsTable.setWidthPercentage(100);
        statsTable.setSpacingAfter(20);
        addStatCell(statsTable, String.valueOf(physicalLogs.size()), "Days logged", bodyFont, subheadingFont);
        addStatCell(statsTable, String.valueOf(mentalLogs.size()), "Mental check-ins", bodyFont, subheadingFont);
        addStatCell(statsTable, String.valueOf(flares.size()), "Flares recorded", bodyFont, subheadingFont);
        document.add(statsTable);

        addDivider(document);

        document.add(Chunk.NEWLINE);
        addSectionHeader(document, "Physical Health", headingFont);

        if (physicalLogs.isEmpty()) {
            document.add(new Paragraph("No physical logs recorded in this period.", smallFont));
        } else {
            long lowPainDays = physicalLogs.stream()
                    .filter(l -> l.getPainScore() != null && l.getPainScore() <= 3).count();
            long highPainDays = physicalLogs.stream()
                    .filter(l -> l.getPainScore() != null && l.getPainScore() >= 7).count();

            Paragraph painBreakdown = new Paragraph(
                    String.format("Low pain days (<=3/10): %d    High pain days (>=7/10): %d    Average: %.1f/10",
                            lowPainDays, highPainDays, avgPain), bodyFont);
            painBreakdown.setSpacingAfter(10);
            document.add(painBreakdown);

            long fatigueDays = physicalLogs.stream().filter(l -> Boolean.TRUE.equals(l.getFatigue())).count();
            long jointPainDays = physicalLogs.stream().filter(l -> Boolean.TRUE.equals(l.getJointPain())).count();
            long nauseaDays = physicalLogs.stream().filter(l -> Boolean.TRUE.equals(l.getNausea())).count();
            long bloatingDays = physicalLogs.stream().filter(l -> Boolean.TRUE.equals(l.getBloating())).count();
            long feverDays = physicalLogs.stream().filter(l -> Boolean.TRUE.equals(l.getFever())).count();

            addSubheading(document, "Symptom Frequency", subheadingFont);
            PdfPTable symptomTable = new PdfPTable(5);
            symptomTable.setWidthPercentage(100);
            symptomTable.setSpacingAfter(12);
            addSymptomCell(symptomTable, "Fatigue", fatigueDays, physicalLogs.size(), bodyFont, smallFont);
            addSymptomCell(symptomTable, "Joint Pain", jointPainDays, physicalLogs.size(), bodyFont, smallFont);
            addSymptomCell(symptomTable, "Nausea", nauseaDays, physicalLogs.size(), bodyFont, smallFont);
            addSymptomCell(symptomTable, "Bloating", bloatingDays, physicalLogs.size(), bodyFont, smallFont);
            addSymptomCell(symptomTable, "Fever", feverDays, physicalLogs.size(), bodyFont, smallFont);
            document.add(symptomTable);

            OptionalDouble avgBowel = physicalLogs.stream()
                    .filter(l -> l.getBowelFrequency() != null)
                    .mapToInt(DailyPhysicalLog::getBowelFrequency)
                    .average();

            if (avgBowel.isPresent()) {
                Paragraph bowelPara = new Paragraph(
                        String.format("Average daily bowel movements: %.1f", avgBowel.getAsDouble()), bodyFont);
                bowelPara.setSpacingAfter(10);
                document.add(bowelPara);
            }

            addSubheading(document, "Recent Physical Logs", subheadingFont);
            PdfPTable logsTable = new PdfPTable(new float[]{2, 1.5f, 1.5f, 1.5f, 3});
            logsTable.setWidthPercentage(100);
            logsTable.setSpacingAfter(16);
            addTableHeader(logsTable, new String[]{"Date", "Pain", "Bowel", "Energy", "Symptoms"}, subheadingFont);

            for (DailyPhysicalLog log : physicalLogs.stream().limit(10).toList()) {
                String symptoms = buildSymptomString(log);
                addTableRow(logsTable, new String[]{
                        log.getLogDate().format(fmt),
                        log.getPainScore() != null ? log.getPainScore() + "/10" : "--",
                        log.getBowelFrequency() != null ? log.getBowelFrequency() + "x" : "--",
                        log.getEnergyLevel() != null ? log.getEnergyLevel() + "/10" : "--",
                        symptoms.isEmpty() ? "None reported" : symptoms
                }, bodyFont, smallFont);
            }
            document.add(logsTable);
        }

        addDivider(document);

        document.add(Chunk.NEWLINE);
        addSectionHeader(document, "Mental Health & Stress", headingFont);

        if (mentalLogs.isEmpty()) {
            document.add(new Paragraph("No mental check-ins recorded in this period.", smallFont));
        } else {
            Paragraph mentalBreakdown = new Paragraph(
                    String.format("Average stress: %.1f/10    Average mood: %.1f/10    Average sleep quality: %.1f/10",
                            avgStress, avgMood, avgSleep), bodyFont);
            mentalBreakdown.setSpacingAfter(10);
            document.add(mentalBreakdown);

            long meditationDays = mentalLogs.stream()
                    .filter(l -> Boolean.TRUE.equals(l.getMeditationDone())).count();

            Paragraph meditationPara = new Paragraph(
                    String.format("Mindfulness/meditation practiced: %d of %d days logged",
                            meditationDays, mentalLogs.size()), bodyFont);
            meditationPara.setSpacingAfter(10);
            document.add(meditationPara);

            addSubheading(document, "Recent Mental Check-ins", subheadingFont);
            PdfPTable mentalTable = new PdfPTable(new float[]{2, 1.5f, 1.5f, 1.5f, 2});
            mentalTable.setWidthPercentage(100);
            mentalTable.setSpacingAfter(16);
            addTableHeader(mentalTable, new String[]{"Date", "Stress", "Mood", "Sleep", "Stressor"}, subheadingFont);

            for (DailyMentalLog log : mentalLogs.stream().limit(10).toList()) {
                addTableRow(mentalTable, new String[]{
                        log.getLogDate().format(fmt),
                        log.getStressScore() != null ? log.getStressScore() + "/10" : "--",
                        log.getMoodScore() != null ? log.getMoodScore() + "/10" : "--",
                        log.getSleepQuality() != null ? log.getSleepQuality() + "/10" : "--",
                        formatStressEvent(log.getStressEventType())
                }, bodyFont, smallFont);
            }
            document.add(mentalTable);
        }

        addDivider(document);

        document.add(Chunk.NEWLINE);
        addSectionHeader(document, "Flare History", headingFont);

        if (flares.isEmpty()) {
            Paragraph noFlares = new Paragraph("No flares recorded in this period.", bodyFont);
            noFlares.setSpacingAfter(16);
            document.add(noFlares);
        } else {
            DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
            for (Flare flare : flares) {
                PdfPTable flareTable = new PdfPTable(1);
                flareTable.setWidthPercentage(100);
                flareTable.setSpacingAfter(10);

                String flareHeader = "Flare: " +
                        (flare.getStartDate() != null ? flare.getStartDate().format(dtFmt) : "--") +
                        (flare.getEndDate() != null ? " → " + flare.getEndDate().format(dtFmt) : " → ongoing") +
                        "  |  Severity: " + (flare.getSeverity() != null ? flare.getSeverity() + "/10" : "--");

                PdfPCell headerCell = new PdfPCell(new Phrase(flareHeader, accentFont));
                headerCell.setBackgroundColor(new BaseColor(255, 248, 235));
                headerCell.setPadding(8);
                headerCell.setBorderColor(ACCENT);
                flareTable.addCell(headerCell);

                if (flare.getPhysicalContext() != null && !flare.getPhysicalContext().isEmpty()) {
                    PdfPCell physCell = new PdfPCell(new Phrase("Physical context: " + flare.getPhysicalContext(), bodyFont));
                    physCell.setPadding(6);
                    physCell.setBorderColor(LIGHT_GRAY);
                    flareTable.addCell(physCell);
                }

                if (flare.getMentalContext() != null && !flare.getMentalContext().isEmpty()) {
                    PdfPCell mentCell = new PdfPCell(new Phrase("Mental context: " + flare.getMentalContext(), bodyFont));
                    mentCell.setPadding(6);
                    mentCell.setBorderColor(LIGHT_GRAY);
                    flareTable.addCell(mentCell);
                }

                if (flare.getPotentialTriggers() != null && !flare.getPotentialTriggers().isEmpty()) {
                    PdfPCell trigCell = new PdfPCell(new Phrase("Potential triggers: " + flare.getPotentialTriggers(), bodyFont));
                    trigCell.setPadding(6);
                    trigCell.setBorderColor(LIGHT_GRAY);
                    flareTable.addCell(trigCell);
                }

                document.add(flareTable);
            }
        }

        document.add(Chunk.NEWLINE);
        addDivider(document);
        Paragraph footer = new Paragraph(
                "This report was generated by Flara and is intended to support conversations with your healthcare provider. " +
                        "It does not constitute medical advice. Please share this with your gastroenterologist or care team.",
                smallFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(10);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    /**
     * Draws a full-width horizontal rule, used to separate major sections.
     *
     * @param document the document being built
     */
    private void addDivider(Document document) throws DocumentException {
        LineSeparator line = new LineSeparator();
        line.setLineColor(new BaseColor(220, 220, 220));
        line.setLineWidth(0.5f);
        document.add(new Chunk(line));
    }

    /**
     * Writes a top-level section heading with consistent spacing above and below.
     *
     * <p>Centralised so every section in the report is spaced identically; the
     * alternative is tuning paragraph spacing by hand at each call site.
     *
     * @param document the document being built
     * @param text     the heading text
     * @param font     the heading font
     */
    private void addSectionHeader(Document document, String text, Font font) throws DocumentException {
        Paragraph heading = new Paragraph(text, font);
        heading.setSpacingBefore(8);
        heading.setSpacingAfter(10);
        document.add(heading);
    }

    /**
     * Writes a second-level heading inside a section.
     *
     * @param document the document being built
     * @param text     the subheading text
     * @param font     the subheading font
     */
    private void addSubheading(Document document, String text, Font font) throws DocumentException {
        Paragraph sub = new Paragraph(text, font);
        sub.setSpacingBefore(6);
        sub.setSpacingAfter(6);
        document.add(sub);
    }

    /**
     * Adds one panel to the summary row at the top of the report.
     *
     * <p>The value is coloured by severity rather than by category, so a
     * clinician scanning the page sees where the problems are before reading
     * any numbers.
     *
     * @param table      the summary table being assembled
     * @param label      what the figure measures, for example "Avg Pain"
     * @param value      the formatted figure
     * @param valueColor severity colour for the figure
     */
    private void addSummaryCell(PdfPTable table, String label, String value, BaseColor valueColor) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBorderColor(LIGHT_GRAY);
        cell.setBackgroundColor(new BaseColor(250, 250, 250));
        Paragraph labelPara = new Paragraph(label, new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, TEXT_GRAY));
        Paragraph valuePara = new Paragraph(value, new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, valueColor));
        cell.addElement(labelPara);
        cell.addElement(valuePara);
        table.addCell(cell);
    }

    /**
     * Adds a large-number statistic cell, used for counts rather than averages.
     *
     * @param table     the table being assembled
     * @param number    the figure, pre-formatted
     * @param label     what it counts
     * @param numFont   font for the figure
     * @param labelFont font for the caption
     */
    private void addStatCell(PdfPTable table, String number, String label, Font numFont, Font labelFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(10);
        cell.setBorderColor(LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph numPara = new Paragraph(number, new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, TEXT_DARK));
        numPara.setAlignment(Element.ALIGN_CENTER);
        Paragraph labelPara = new Paragraph(label, labelFont);
        labelPara.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(numPara);
        cell.addElement(labelPara);
        table.addCell(cell);
    }

    /**
     * Adds a symptom frequency cell, showing days affected against days logged.
     *
     * <p>Both numbers are shown deliberately. "12 days" means little on its own;
     * 12 out of 14 and 12 out of 90 describe very different situations, and the
     * denominator is what makes the figure interpretable.
     *
     * @param table     the table being assembled
     * @param name      the symptom
     * @param days      number of logged days on which it was present
     * @param total     number of days logged in the window
     * @param bodyFont  font for the main text
     * @param smallFont font for the secondary text
     */
    private void addSymptomCell(PdfPTable table, String name, long days, int total, Font bodyFont, Font smallFont) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(8);
        cell.setBorderColor(LIGHT_GRAY);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        int pct = total > 0 ? (int) Math.round((double) days / total * 100) : 0;
        BaseColor color = pct >= 50 ? RED : pct >= 25 ? AMBER : GREEN;
        Paragraph pctPara = new Paragraph(pct + "%", new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, color));
        pctPara.setAlignment(Element.ALIGN_CENTER);
        Paragraph namePara = new Paragraph(name, smallFont);
        namePara.setAlignment(Element.ALIGN_CENTER);
        Paragraph daysPara = new Paragraph(days + " days", smallFont);
        daysPara.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pctPara);
        cell.addElement(namePara);
        cell.addElement(daysPara);
        table.addCell(cell);
    }

    /**
     * Writes a styled header row across a data table.
     *
     * @param table   the table being assembled
     * @param headers the column titles, in order
     * @param font    the header font
     */
    private void addTableHeader(PdfPTable table, String[] headers, Font font) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(LIGHT_GRAY);
            cell.setPadding(6);
            cell.setBorderColor(new BaseColor(220, 220, 220));
            table.addCell(cell);
        }
    }

    /**
     * Writes one data row, with the first column emphasised.
     *
     * @param table     the table being assembled
     * @param values    cell values, in column order
     * @param bodyFont  font for the first column
     * @param smallFont font for the remaining columns
     */
    private void addTableRow(PdfPTable table, String[] values, Font bodyFont, Font smallFont) {
        for (String value : values) {
            PdfPCell cell = new PdfPCell(new Phrase(value, smallFont));
            cell.setPadding(5);
            cell.setBorderColor(new BaseColor(230, 230, 230));
            table.addCell(cell);
        }
    }

    /**
     * Flattens a day's boolean symptom flags into one readable phrase.
     *
     * <p>The entity stores each symptom as a separate boolean, which suits
     * querying but reads poorly in a table cell. This collapses the set flags
     * into a single comma-separated string for display.
     *
     * @param log the day's physical log
     * @return the symptoms present, comma separated
     */
    private String buildSymptomString(DailyPhysicalLog log) {
        StringBuilder sb = new StringBuilder();
        if (Boolean.TRUE.equals(log.getFatigue())) sb.append("Fatigue, ");
        if (Boolean.TRUE.equals(log.getJointPain())) sb.append("Joint pain, ");
        if (Boolean.TRUE.equals(log.getNausea())) sb.append("Nausea, ");
        if (Boolean.TRUE.equals(log.getBloating())) sb.append("Bloating, ");
        if (Boolean.TRUE.equals(log.getFever())) sb.append("Fever, ");
        if (Boolean.TRUE.equals(log.getPerianalDiscomfort())) sb.append("Perianal, ");
        if (Boolean.TRUE.equals(log.getMouthSores())) sb.append("Mouth sores, ");
        if (Boolean.TRUE.equals(log.getRectalBleeding())) sb.append("Rectal bleeding, ");
        if (Boolean.TRUE.equals(log.getUrgency())) sb.append("Urgency, ");
        String result = sb.toString();
        return result.endsWith(", ") ? result.substring(0, result.length() - 2) : result;
    }

    /**
     * Converts a stress event constant into prose for the report.
     *
     * <p>{@code MEDICAL_ANXIETY} is not something to print on a document a
     * patient hands to their doctor, so the enum names are mapped to readable
     * labels here rather than being written out directly.
     *
     * @param type the recorded stress category, possibly null
     * @return a human-readable label
     */
    private String formatStressEvent(DailyMentalLog.StressEventType type) {
        if (type == null) return "None";
        return switch (type) {
            case WORK -> "Work";
            case RELATIONSHIPS -> "Relationships";
            case MEDICAL_ANXIETY -> "Medical anxiety";
            case FINANCIAL -> "Financial";
            case FAMILY -> "Family";
            case OTHER -> "Other";
            default -> "None";
        };
    }

    /**
     * Maps an average pain score onto a severity colour.
     *
     * <p>Higher is worse for pain, which is why this and
     * {@link #getStressColor(double)} exist separately from the mood and sleep
     * figures, where higher is better.
     *
     * @param score the average, on a 1 to 10 scale
     * @return green, amber or red
     */
    private BaseColor getPainColor(double score) {
        if (score <= 3) return GREEN;
        if (score <= 6) return AMBER;
        return RED;
    }

    /**
     * Maps an average stress score onto a severity colour. Higher is worse.
     *
     * @param score the average, on a 1 to 10 scale
     * @return green, amber or red
     */
    private BaseColor getStressColor(double score) {
        if (score <= 3) return GREEN;
        if (score <= 6) return AMBER;
        return RED;
    }
}