package com.skywings.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;
import com.skywings.entity.Booking;
import com.skywings.entity.BookingPassenger;
import com.skywings.entity.Flight;
import com.skywings.entity.Payment;
import com.skywings.repository.BookingPassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final BookingPassengerRepository bookingPassengerRepository;

    // Brand colors — calm zen-inspired palette
    private static final Color NAVY = new Color(30, 58, 95);
    private static final Color NAVY_LIGHT = new Color(42, 77, 122);
    private static final Color SKY_BLUE = new Color(219, 234, 254);
    private static final Color SOFT_BLUE = new Color(239, 246, 255);
    private static final Color ACCENT_GREEN = new Color(16, 185, 129);
    private static final Color WARM_GRAY = new Color(107, 114, 128);
    private static final Color LIGHT_GRAY = new Color(243, 244, 246);
    private static final Color DIVIDER = new Color(229, 231, 235);
    private static final Color WHITE = Color.WHITE;

    // Fonts
    private static final Font BRAND_TITLE = new Font(Font.HELVETICA, 22, Font.BOLD, WHITE);
    private static final Font BRAND_SUB = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(191, 219, 254));
    private static final Font SECTION_TITLE = new Font(Font.HELVETICA, 11, Font.BOLD, NAVY);
    private static final Font LABEL = new Font(Font.HELVETICA, 8, Font.NORMAL, WARM_GRAY);
    private static final Font VALUE = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(31, 41, 55));
    private static final Font VALUE_LARGE = new Font(Font.HELVETICA, 20, Font.BOLD, NAVY);
    private static final Font BODY = new Font(Font.HELVETICA, 9, Font.NORMAL, new Color(55, 65, 81));
    private static final Font SMALL = new Font(Font.HELVETICA, 7, Font.NORMAL, WARM_GRAY);
    private static final Font STATUS_FONT = new Font(Font.HELVETICA, 9, Font.BOLD, WHITE);
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 7, Font.ITALIC, WARM_GRAY);

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.ofPattern("EEE, dd MMM");

    public byte[] generateETicketPdf(Booking booking) {
        try {
            Flight flight = booking.getFlight();
            List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(booking.getId());
            Payment payment = booking.getPayment();
            String txnId = payment != null ? payment.getTransactionId() : "N/A";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            // ═══════════════════════════════════════════════
            // HEADER — Navy branded banner
            // ═══════════════════════════════════════════════
            PdfPTable header = new PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[]{3, 1});

            // Left: Brand name + subtitle
            PdfPCell brandCell = new PdfPCell();
            brandCell.setBackgroundColor(NAVY);
            brandCell.setPadding(20);
            brandCell.setBorder(0);
            brandCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            Paragraph brandName = new Paragraph();
            brandName.add(new Chunk("✈  ", new Font(Font.HELVETICA, 20, Font.NORMAL, WHITE)));
            brandName.add(new Chunk("SKYWINGS AIRWAYS", BRAND_TITLE));
            brandCell.addElement(brandName);
            brandCell.addElement(new Paragraph("Electronic Ticket & Itinerary Receipt", BRAND_SUB));
            header.addCell(brandCell);

            // Right: Status badge
            PdfPCell statusCell = new PdfPCell();
            statusCell.setBackgroundColor(NAVY);
            statusCell.setPadding(20);
            statusCell.setBorder(0);
            statusCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            statusCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            String status = booking.getStatus().name();
            Paragraph statusPara = new Paragraph();
            statusPara.setAlignment(Element.ALIGN_RIGHT);
            Chunk statusChunk = new Chunk("  " + status + "  ", STATUS_FONT);
            statusChunk.setBackground("CONFIRMED".equals(status) ? ACCENT_GREEN : new Color(245, 158, 11));
            statusPara.add(statusChunk);
            statusCell.addElement(statusPara);

            Paragraph txnPara = new Paragraph(txnId, new Font(Font.COURIER, 10, Font.BOLD, new Color(147, 197, 253)));
            txnPara.setAlignment(Element.ALIGN_RIGHT);
            txnPara.setSpacingBefore(8);
            statusCell.addElement(txnPara);
            header.addCell(statusCell);

            doc.add(header);
            addSpacer(doc, 8);

            // ═══════════════════════════════════════════════
            // FLIGHT INFO — Route card
            // ═══════════════════════════════════════════════
            PdfPTable routeCard = new PdfPTable(3);
            routeCard.setWidthPercentage(100);
            routeCard.setWidths(new float[]{2, 1, 2});

            // Departure
            PdfPCell depCell = createInfoCell(Element.ALIGN_LEFT);
            depCell.setBackgroundColor(SOFT_BLUE);
            depCell.addElement(new Paragraph("DEPARTURE", LABEL));
            depCell.addElement(new Paragraph(flight.getDepartureTime().format(TIME_FMT), VALUE_LARGE));
            depCell.addElement(new Paragraph(flight.getOriginCode(), new Font(Font.HELVETICA, 14, Font.BOLD, NAVY)));
            depCell.addElement(new Paragraph(flight.getOrigin(), BODY));
            depCell.addElement(new Paragraph(flight.getDepartureTime().format(DAY_FMT), LABEL));
            routeCard.addCell(depCell);

            // Middle — flight info
            PdfPCell midCell = createInfoCell(Element.ALIGN_CENTER);
            midCell.setBackgroundColor(SOFT_BLUE);
            midCell.addElement(spacerParagraph(10));
            Paragraph flightNumPara = new Paragraph(flight.getFlightNumber(), new Font(Font.HELVETICA, 9, Font.BOLD, NAVY));
            flightNumPara.setAlignment(Element.ALIGN_CENTER);
            midCell.addElement(flightNumPara);

            Paragraph arrowPara = new Paragraph("———  ✈  ———", new Font(Font.HELVETICA, 8, Font.NORMAL, WARM_GRAY));
            arrowPara.setAlignment(Element.ALIGN_CENTER);
            arrowPara.setSpacingBefore(5);
            midCell.addElement(arrowPara);

            long durationMins = java.time.Duration.between(flight.getDepartureTime(), flight.getArrivalTime()).toMinutes();
            String durStr = String.format("%dh %dm", durationMins / 60, durationMins % 60);
            Paragraph durPara = new Paragraph(durStr, new Font(Font.HELVETICA, 8, Font.NORMAL, WARM_GRAY));
            durPara.setAlignment(Element.ALIGN_CENTER);
            durPara.setSpacingBefore(3);
            midCell.addElement(durPara);

            Paragraph typePara = new Paragraph(flight.getFlightType(), LABEL);
            typePara.setAlignment(Element.ALIGN_CENTER);
            typePara.setSpacingBefore(3);
            midCell.addElement(typePara);
            routeCard.addCell(midCell);

            // Arrival
            PdfPCell arrCell = createInfoCell(Element.ALIGN_RIGHT);
            arrCell.setBackgroundColor(SOFT_BLUE);
            Paragraph arrLabel = new Paragraph("ARRIVAL", LABEL);
            arrLabel.setAlignment(Element.ALIGN_RIGHT);
            arrCell.addElement(arrLabel);
            Paragraph arrTime = new Paragraph(flight.getArrivalTime().format(TIME_FMT), VALUE_LARGE);
            arrTime.setAlignment(Element.ALIGN_RIGHT);
            arrCell.addElement(arrTime);
            Paragraph arrCode = new Paragraph(flight.getDestCode(), new Font(Font.HELVETICA, 14, Font.BOLD, NAVY));
            arrCode.setAlignment(Element.ALIGN_RIGHT);
            arrCell.addElement(arrCode);
            Paragraph arrCity = new Paragraph(flight.getDestination(), BODY);
            arrCity.setAlignment(Element.ALIGN_RIGHT);
            arrCell.addElement(arrCity);
            Paragraph arrDate = new Paragraph(flight.getArrivalTime().format(DAY_FMT), LABEL);
            arrDate.setAlignment(Element.ALIGN_RIGHT);
            arrCell.addElement(arrDate);
            routeCard.addCell(arrCell);

            doc.add(routeCard);
            addSpacer(doc, 8);

            // ═══════════════════════════════════════════════
            // BOOKING DETAILS — Class, Fare, Issued
            // ═══════════════════════════════════════════════
            PdfPTable detailsRow = new PdfPTable(4);
            detailsRow.setWidthPercentage(100);
            detailsRow.setWidths(new float[]{1, 1, 1, 1});

            addLabelValueCell(detailsRow, "CLASS", booking.getSeatClass().name());
            addLabelValueCell(detailsRow, "PASSENGERS", String.valueOf(booking.getNumSeats()));
            String fareLabel = booking.getFareType() != null && !"REGULAR".equals(booking.getFareType())
                ? booking.getFareType().replace("_", " ") : "Regular";
            addLabelValueCell(detailsRow, "FARE TYPE", fareLabel);
            addLabelValueCell(detailsRow, "ISSUED", booking.getBookingDate().format(DATE_FMT));
            doc.add(detailsRow);
            addSpacer(doc, 8);

            // ═══════════════════════════════════════════════
            // PASSENGERS TABLE
            // ═══════════════════════════════════════════════
            doc.add(sectionTitle("PASSENGERS"));
            addSpacer(doc, 4);

            PdfPTable paxTable = new PdfPTable(5);
            paxTable.setWidthPercentage(100);
            paxTable.setWidths(new float[]{3, 1, 1.5f, 1.5f, 3});

            // Header row
            for (String h : new String[]{"Name", "Age", "Seat", "Gender", "Details"}) {
                PdfPCell hCell = new PdfPCell(new Phrase(h, new Font(Font.HELVETICA, 8, Font.BOLD, NAVY)));
                hCell.setBackgroundColor(SKY_BLUE);
                hCell.setPadding(6);
                hCell.setBorderColor(DIVIDER);
                hCell.setBorderWidth(0.5f);
                paxTable.addCell(hCell);
            }

            // Data rows
            boolean alt = false;
            for (BookingPassenger bp : passengers) {
                Color rowBg = alt ? LIGHT_GRAY : WHITE;
                addPaxCell(paxTable, bp.getPassengerName(), rowBg);
                addPaxCell(paxTable, String.valueOf(bp.getAge()), rowBg);
                addPaxCell(paxTable, bp.getSeat() != null ? bp.getSeat().getSeatNumber() : "—", rowBg);
                addPaxCell(paxTable, bp.getGender() != null ? bp.getGender() : "—", rowBg);

                StringBuilder details = new StringBuilder();
                if (bp.getPassportNumber() != null) details.append("Passport: ").append(bp.getPassportNumber());
                if (bp.getNationality() != null) {
                    if (!details.isEmpty()) details.append(" · ");
                    details.append(bp.getNationality());
                }
                if (bp.getMealPreference() != null && !"NO_PREFERENCE".equals(bp.getMealPreference())) {
                    if (!details.isEmpty()) details.append(" · ");
                    details.append("🍽 ").append(bp.getMealPreference());
                }
                if (bp.getSpecialAssistance() != null && !"NONE".equals(bp.getSpecialAssistance())) {
                    if (!details.isEmpty()) details.append(" · ");
                    details.append(bp.getSpecialAssistance().replace("_", " "));
                }
                if (Boolean.TRUE.equals(bp.getIsSeniorCitizen())) {
                    if (!details.isEmpty()) details.append(" · ");
                    details.append("Senior Citizen");
                }
                addPaxCell(paxTable, !details.isEmpty() ? details.toString() : "—", rowBg);
                alt = !alt;
            }
            doc.add(paxTable);
            addSpacer(doc, 10);

            // ═══════════════════════════════════════════════
            // FARE & QR CODE side by side
            // ═══════════════════════════════════════════════
            PdfPTable bottomSection = new PdfPTable(2);
            bottomSection.setWidthPercentage(100);
            bottomSection.setWidths(new float[]{3, 2});

            // Left: Fare breakdown
            PdfPCell fareCell = new PdfPCell();
            fareCell.setBorder(0);
            fareCell.setPadding(10);

            fareCell.addElement(sectionTitle("FARE BREAKDOWN"));
            fareCell.addElement(spacerParagraph(5));

            BigDecimal subtotal = booking.getTotalPrice().subtract(booking.getTaxAmount());
            fareCell.addElement(fareRow("Base Fare", "₹" + formatAmount(subtotal)));
            fareCell.addElement(fareRow("Tax & Fees", "₹" + formatAmount(booking.getTaxAmount())));
            if (booking.getFareType() != null && !"REGULAR".equals(booking.getFareType())) {
                fareCell.addElement(fareRow("Discount (" + booking.getFareType().replace("_", " ") + ")", "Applied"));
            }

            // Total with highlight
            PdfPTable totalBox = new PdfPTable(2);
            totalBox.setWidthPercentage(100);
            totalBox.setSpacingBefore(5);
            PdfPCell totalLabel = new PdfPCell(new Phrase("TOTAL", new Font(Font.HELVETICA, 10, Font.BOLD, WHITE)));
            totalLabel.setBackgroundColor(NAVY);
            totalLabel.setPadding(8);
            totalLabel.setBorder(0);
            totalBox.addCell(totalLabel);
            PdfPCell totalValue = new PdfPCell(new Phrase("₹" + formatAmount(booking.getTotalPrice()),
                new Font(Font.HELVETICA, 14, Font.BOLD, WHITE)));
            totalValue.setBackgroundColor(NAVY);
            totalValue.setPadding(8);
            totalValue.setBorder(0);
            totalValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            totalBox.addCell(totalValue);
            fareCell.addElement(totalBox);

            // Payment info
            if (payment != null) {
                fareCell.addElement(spacerParagraph(8));
                fareCell.addElement(new Paragraph("Payment: ****" + payment.getCardLastFour() +
                    " (" + payment.getPaymentMethod().name().replace("_", " ") + ")", SMALL));
                fareCell.addElement(new Paragraph("Transaction: " + txnId, SMALL));
            }
            bottomSection.addCell(fareCell);

            // Right: QR Code
            PdfPCell qrCell = new PdfPCell();
            qrCell.setBorder(0);
            qrCell.setPadding(10);
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

            Paragraph qrTitle = new Paragraph("SCAN FOR DETAILS", LABEL);
            qrTitle.setAlignment(Element.ALIGN_CENTER);
            qrCell.addElement(qrTitle);
            qrCell.addElement(spacerParagraph(5));

            byte[] qrImage = generateQrCode(booking, passengers);
            Image qr = Image.getInstance(qrImage);
            qr.scaleToFit(130, 130);
            qr.setAlignment(Element.ALIGN_CENTER);
            qrCell.addElement(qr);

            Paragraph scanHint = new Paragraph("Scan with phone camera", SMALL);
            scanHint.setAlignment(Element.ALIGN_CENTER);
            scanHint.setSpacingBefore(4);
            qrCell.addElement(scanHint);
            bottomSection.addCell(qrCell);

            doc.add(bottomSection);
            addSpacer(doc, 12);

            // ═══════════════════════════════════════════════
            // FOOTER — Branding + disclaimer
            // ═══════════════════════════════════════════════
            PdfPTable footer = new PdfPTable(1);
            footer.setWidthPercentage(100);
            PdfPCell footerCell = new PdfPCell();
            footerCell.setBackgroundColor(LIGHT_GRAY);
            footerCell.setBorder(0);
            footerCell.setPadding(12);

            Paragraph footerLine1 = new Paragraph("✈ SkyWings Airways — Your journey, our wings",
                new Font(Font.HELVETICA, 8, Font.BOLD, NAVY));
            footerLine1.setAlignment(Element.ALIGN_CENTER);
            footerCell.addElement(footerLine1);

            Paragraph footerLine2 = new Paragraph(
                "This is a simulated booking for demonstration purposes. No real payment was charged. " +
                "Built with Spring Boot, React, PostgreSQL, and Redis.", FOOTER_FONT);
            footerLine2.setAlignment(Element.ALIGN_CENTER);
            footerLine2.setSpacingBefore(4);
            footerCell.addElement(footerLine2);

            Paragraph footerLine3 = new Paragraph("skywings-airways.vercel.app | Powered by SerpAPI Google Flights", FOOTER_FONT);
            footerLine3.setAlignment(Element.ALIGN_CENTER);
            footerLine3.setSpacingBefore(2);
            footerCell.addElement(footerLine3);
            footer.addCell(footerCell);

            doc.add(footer);

            doc.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate e-ticket PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to generate e-ticket", e);
        }
    }

    // ─── QR Code with full booking JSON ───────────────────

    private byte[] generateQrCode(Booking booking, List<BookingPassenger> passengers) throws Exception {
        Flight flight = booking.getFlight();
        Payment payment = booking.getPayment();

        // Build a rich JSON payload
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"txn\":\"").append(payment != null ? payment.getTransactionId() : "N/A").append("\",");
        json.append("\"flight\":\"").append(flight.getFlightNumber()).append("\",");
        json.append("\"route\":\"").append(flight.getOriginCode()).append(" → ").append(flight.getDestCode()).append("\",");
        json.append("\"departure\":\"").append(flight.getDepartureTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))).append("\",");
        json.append("\"arrival\":\"").append(flight.getArrivalTime().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))).append("\",");
        json.append("\"class\":\"").append(booking.getSeatClass().name()).append("\",");
        json.append("\"pax\":").append(booking.getNumSeats()).append(",");
        json.append("\"total\":\"INR ").append(formatAmount(booking.getTotalPrice())).append("\",");
        json.append("\"status\":\"").append(booking.getStatus().name()).append("\",");
        json.append("\"passengers\":[");
        for (int i = 0; i < passengers.size(); i++) {
            BookingPassenger bp = passengers.get(i);
            if (i > 0) json.append(",");
            json.append("{\"name\":\"").append(bp.getPassengerName()).append("\"");
            json.append(",\"seat\":\"").append(bp.getSeat() != null ? bp.getSeat().getSeatNumber() : "—").append("\"");
            json.append("}");
        }
        json.append("]}");

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(json.toString(), BarcodeFormat.QR_CODE, 400, 400);

        // QR in navy color instead of black
        MatrixToImageConfig config = new MatrixToImageConfig(NAVY.getRGB(), WHITE.getRGB());
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix, config);

        ByteArrayOutputStream imgOut = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", imgOut);
        return imgOut.toByteArray();
    }

    // ─── Helper methods ──────────────────────────────────

    private PdfPCell createInfoCell(int alignment) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(0);
        cell.setPadding(15);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private void addLabelValueCell(PdfPTable table, String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(LIGHT_GRAY);
        cell.setBorder(0);
        cell.setPadding(8);
        cell.addElement(new Paragraph(label, LABEL));
        cell.addElement(new Paragraph(value, VALUE));
        table.addCell(cell);
    }

    private void addPaxCell(PdfPTable table, String text, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text, BODY));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setBorderColor(DIVIDER);
        cell.setBorderWidth(0.5f);
        table.addCell(cell);
    }

    private Paragraph sectionTitle(String text) {
        Paragraph p = new Paragraph();
        Chunk dot = new Chunk("●  ", new Font(Font.HELVETICA, 8, Font.NORMAL, ACCENT_GREEN));
        p.add(dot);
        p.add(new Chunk(text, SECTION_TITLE));
        return p;
    }

    private Paragraph fareRow(String label, String value) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(label, BODY));
        p.add(new Chunk("    "));
        p.add(new Chunk(value, VALUE));
        p.setSpacingBefore(3);
        return p;
    }

    private void addSpacer(Document doc, float height) throws DocumentException {
        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingBefore(height);
        doc.add(spacer);
    }

    private Paragraph spacerParagraph(float height) {
        Paragraph p = new Paragraph(" ");
        p.setSpacingBefore(height);
        return p;
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%,.2f", amount);
    }
}
