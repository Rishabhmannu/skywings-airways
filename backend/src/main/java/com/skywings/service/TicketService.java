package com.skywings.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.skywings.entity.Booking;
import com.skywings.entity.BookingPassenger;
import com.skywings.entity.Flight;
import com.skywings.repository.BookingPassengerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final BookingPassengerRepository bookingPassengerRepository;

    private static final Color BRAND_COLOR = new Color(30, 58, 95);
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 18, Font.BOLD, BRAND_COLOR);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, BRAND_COLOR);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);
    private static final Font SMALL_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC, Color.GRAY);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    public byte[] generateETicketPdf(Booking booking) {
        try {
            Flight flight = booking.getFlight();
            List<BookingPassenger> passengers = bookingPassengerRepository.findByBookingId(booking.getId());
            String transactionId = booking.getPayment() != null
                ? booking.getPayment().getTransactionId() : "N/A";

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 40, 40, 40, 40);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Title
            Paragraph title = new Paragraph("SKYWINGS AIRWAYS", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph subtitle = new Paragraph("Electronic Ticket / Itinerary Receipt",
                new Font(Font.HELVETICA, 11, Font.NORMAL, Color.GRAY));
            subtitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subtitle);
            document.add(new Paragraph("\n"));

            // Booking info
            document.add(new Paragraph("Booking Reference: " + transactionId, HEADER_FONT));
            document.add(new Paragraph("Date of Issue: " +
                booking.getBookingDate().format(DATE_FMT), NORMAL_FONT));
            document.add(new Paragraph("Status: " + booking.getStatus().name(), NORMAL_FONT));
            document.add(new Paragraph("\n"));

            // Flight details
            document.add(new Paragraph("FLIGHT DETAILS", HEADER_FONT));
            PdfPTable flightTable = new PdfPTable(2);
            flightTable.setWidthPercentage(100);
            flightTable.setSpacingBefore(5);
            addCell(flightTable, "Flight", flight.getFlightNumber());
            addCell(flightTable, "Route", flight.getOrigin() + " (" + flight.getOriginCode() +
                ") → " + flight.getDestination() + " (" + flight.getDestCode() + ")");
            addCell(flightTable, "Date", flight.getDepartureTime().format(DATE_FMT));
            addCell(flightTable, "Departure", flight.getDepartureTime().format(TIME_FMT));
            addCell(flightTable, "Arrival", flight.getArrivalTime().format(TIME_FMT));
            addCell(flightTable, "Type", flight.getFlightType());
            addCell(flightTable, "Class", booking.getSeatClass().name());
            if (booking.getFareType() != null && !"REGULAR".equals(booking.getFareType())) {
                addCell(flightTable, "Fare Type", booking.getFareType().replace("_", " "));
            }
            document.add(flightTable);
            document.add(new Paragraph("\n"));

            // Passengers
            document.add(new Paragraph("PASSENGER(S)", HEADER_FONT));
            PdfPTable paxTable = new PdfPTable(5);
            paxTable.setWidthPercentage(100);
            paxTable.setSpacingBefore(5);
            paxTable.setWidths(new float[]{3, 1, 1.5f, 2, 2.5f});

            addHeaderCell(paxTable, "Name");
            addHeaderCell(paxTable, "Age");
            addHeaderCell(paxTable, "Seat");
            addHeaderCell(paxTable, "Gender");
            addHeaderCell(paxTable, "Details");

            for (BookingPassenger bp : passengers) {
                paxTable.addCell(new Phrase(bp.getPassengerName(), NORMAL_FONT));
                paxTable.addCell(new Phrase(String.valueOf(bp.getAge()), NORMAL_FONT));
                paxTable.addCell(new Phrase(
                    bp.getSeat() != null ? bp.getSeat().getSeatNumber() : "—", NORMAL_FONT));
                paxTable.addCell(new Phrase(
                    bp.getGender() != null ? bp.getGender() : "—", NORMAL_FONT));

                // Build details string from optional fields
                StringBuilder details = new StringBuilder();
                if (bp.getPassportNumber() != null) details.append("PP: ").append(bp.getPassportNumber());
                if (bp.getNationality() != null) {
                    if (!details.isEmpty()) details.append(" | ");
                    details.append(bp.getNationality());
                }
                if (bp.getMealPreference() != null && !"NO_PREFERENCE".equals(bp.getMealPreference())) {
                    if (!details.isEmpty()) details.append(" | ");
                    details.append("Meal: ").append(bp.getMealPreference());
                }
                if (bp.getSpecialAssistance() != null && !"NONE".equals(bp.getSpecialAssistance())) {
                    if (!details.isEmpty()) details.append(" | ");
                    details.append("Assist: ").append(bp.getSpecialAssistance());
                }
                if (Boolean.TRUE.equals(bp.getIsSeniorCitizen())) {
                    if (!details.isEmpty()) details.append(" | ");
                    details.append("Senior Citizen");
                }
                paxTable.addCell(new Phrase(
                    !details.isEmpty() ? details.toString() : "—", SMALL_FONT));
            }
            document.add(paxTable);
            document.add(new Paragraph("\n"));

            // Fare breakdown
            document.add(new Paragraph("FARE BREAKDOWN", HEADER_FONT));
            PdfPTable fareTable = new PdfPTable(2);
            fareTable.setWidthPercentage(60);
            fareTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            fareTable.setSpacingBefore(5);
            BigDecimalPair subtotal = new BigDecimalPair(
                booking.getTotalPrice().subtract(booking.getTaxAmount()));
            addCell(fareTable, "Subtotal", "Rs " + subtotal.value);
            addCell(fareTable, "Tax", "Rs " + booking.getTaxAmount());
            addCell(fareTable, "Total", "Rs " + booking.getTotalPrice());
            if (booking.getPenaltyAmount().signum() > 0) {
                addCell(fareTable, "Penalty", "Rs " + booking.getPenaltyAmount());
            }
            document.add(fareTable);
            document.add(new Paragraph("\n"));

            // Payment info
            if (booking.getPayment() != null) {
                document.add(new Paragraph("Payment: ****" + booking.getPayment().getCardLastFour() +
                    " (" + booking.getPayment().getPaymentMethod() + ")", NORMAL_FONT));
                document.add(new Paragraph("Transaction: " + transactionId, NORMAL_FONT));
            }
            document.add(new Paragraph("\n"));

            // QR Code
            byte[] qrImage = generateQrCode(booking);
            Image qr = Image.getInstance(qrImage);
            qr.scaleToFit(100, 100);
            qr.setAlignment(Element.ALIGN_CENTER);
            document.add(qr);
            document.add(new Paragraph("\n"));

            // Disclaimer
            Paragraph disclaimer = new Paragraph(
                "This is a simulated booking for demonstration purposes. No real payment was charged.",
                SMALL_FONT);
            disclaimer.setAlignment(Element.ALIGN_CENTER);
            document.add(disclaimer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Failed to generate e-ticket PDF: {}", e.getMessage());
            throw new RuntimeException("Failed to generate e-ticket", e);
        }
    }

    private byte[] generateQrCode(Booking booking) throws Exception {
        String data = String.format("SKY|%s|%s|%s|%d",
            booking.getPayment() != null ? booking.getPayment().getTransactionId() : booking.getId(),
            booking.getFlight().getFlightNumber(),
            booking.getBookingDate().format(DATE_FMT),
            booking.getNumSeats());

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 300, 300);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    private void addCell(PdfPTable table, String label, String value) {
        table.addCell(new Phrase(label, HEADER_FONT));
        table.addCell(new Phrase(value, NORMAL_FONT));
    }

    private void addHeaderCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, HEADER_FONT));
        cell.setBackgroundColor(new Color(240, 240, 240));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private record BigDecimalPair(java.math.BigDecimal value) {}
}
