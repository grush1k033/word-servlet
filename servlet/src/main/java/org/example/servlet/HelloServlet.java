package org.example.servlet;

import util.DatabaseConnector;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

public class HelloServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        // Получаем параметры из запроса
        String russianWord = request.getParameter("russianWord");
        String englishWord = request.getParameter("englishWord");

        String result = null;

        // Определяем направление перевода
        if (russianWord != null && !russianWord.trim().isEmpty()) {
            // Запрос на перевод с русского на английский
            result = DatabaseConnector.translate(russianWord, "russian", "english");
        } else if (englishWord != null && !englishWord.trim().isEmpty()) {
            // Запрос на перевод с английского на русский (обратный перевод)
            result = DatabaseConnector.translate(englishWord, "english", "russian");
        }

        // Отправляем ответ
        PrintWriter out = response.getWriter();
        out.println(result != null ? result : "Перевод не найден");
        out.close();
    }
}