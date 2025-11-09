package org.example.servlet;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@WebServlet("/WordServlet")
public class WordServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        generateWordDocument(response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        generateWordDocument(response);
    }

    private void generateWordDocument(HttpServletResponse response) throws IOException {
        // Создаем новый Word документ
        XWPFDocument document = new XWPFDocument();

        // Создаем заголовок
        createTitle(document, "Отчет из словаря");

        // Информация о генерации
        createParagraph(document, "Документ сгенерирован: " + new Date());
        createParagraph(document, "Сервис: Переводчик русско-английский словарь");

        // Добавляем раздел с популярными словами
        createHeading(document, "Популярные слова:");
        String[][] words = {
                {"привет", "hello"},
                {"мир", "world"},
                {"программа", "program"},
                {"сервер", "server"},
                {"клиент", "client"}
        };

        // Создаем таблицу со словами
        createWordTable(document, words);

        // Добавляем инструкции
        createHeading(document, "Как использовать:");
        createParagraph(document, "1. Откройте веб-страницу словаря");
        createParagraph(document, "2. Введите слово для перевода");
        createParagraph(document, "3. Получите мгновенный перевод");

        // Настраиваем ответ для скачивания
        setupResponse(response);

        // Записываем документ в выходной поток
        try (OutputStream out = response.getOutputStream()) {
            document.write(out);
        }

        document.close();
    }

    private void createTitle(XWPFDocument document, String titleText) {
        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun titleRun = title.createRun();
        titleRun.setText(titleText);
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.setFontFamily("Arial");
        titleRun.addBreak();
    }

    private void createHeading(XWPFDocument document, String headingText) {
        XWPFParagraph heading = document.createParagraph();
        XWPFRun headingRun = heading.createRun();
        headingRun.setText(headingText);
        headingRun.setBold(true);
        headingRun.setFontSize(14);
        headingRun.addBreak();
    }

    private void createParagraph(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.addBreak();
    }

    private void createWordTable(XWPFDocument document, String[][] data) {
        XWPFTable table = document.createTable();

        // Создаем заголовок таблицы
        XWPFTableRow headerRow = table.getRow(0);
        headerRow.getCell(0).setText("Русское слово");
        headerRow.addNewTableCell().setText("Английский перевод");

        // Добавляем данные
        for (String[] rowData : data) {
            XWPFTableRow row = table.createRow();
            row.getCell(0).setText(rowData[0]);
            row.getCell(1).setText(rowData[1]);
        }

        // Добавляем пустую строку после таблицы
        document.createParagraph();
    }

    private void setupResponse(HttpServletResponse response) {
        // Устанавливаем заголовки для автоматического открытия Word
        String fileName = "dictionary_report_" + System.currentTimeMillis() + ".docx";
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setHeader("Content-Disposition",
                "inline; filename=\"" + encodedFileName + "\"; filename*=UTF-8''" + encodedFileName);
        response.setCharacterEncoding("UTF-8");
    }
}