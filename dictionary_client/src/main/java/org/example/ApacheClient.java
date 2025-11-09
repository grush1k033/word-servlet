package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.awt.Desktop;
import java.io.InputStream;
import java.time.Duration;

public class ApacheClient {
    private static JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        frame = new JFrame("Клиент словаря");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new GridLayout(4, 2, 10, 10));

        // Поля ввода и метки
        JLabel lblRussian = new JLabel("Русское слово:");
        JTextField txtRussian = new JTextField();
        JLabel lblEnglish = new JLabel("Английское слово:");
        JTextField txtEnglish = new JTextField();
        JButton btnToEnglish = new JButton("Перевести на английский");
        JButton btnToRussian = new JButton("Перевести на русский");
        JButton btnGenerateWord = new JButton("Сгенерировать Word отчет");
        JButton btnClear = new JButton("Очистить поля");

        // Добавляем компоненты на форму
        frame.add(lblRussian);
        frame.add(txtRussian);
        frame.add(lblEnglish);
        frame.add(txtEnglish);
        frame.add(btnToEnglish);
        frame.add(btnToRussian);
        frame.add(btnGenerateWord);
        frame.add(btnClear);

        // Обработчик для кнопки перевода на английский
        btnToEnglish.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = txtRussian.getText().trim();
                if (!word.isEmpty()) {
                    String translated = sendRequest("russianWord", word);
                    JOptionPane.showMessageDialog(frame,
                            "Русское: " + word + "\nАнглийский: " + translated,
                            "Результат перевода",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Введите русское слово!",
                            "Ошибка",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Обработчик для кнопки перевода на русский
        btnToRussian.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String word = txtEnglish.getText().trim();
                if (!word.isEmpty()) {
                    String translated = sendRequest("englishWord", word);
                    JOptionPane.showMessageDialog(frame,
                            "Английское: " + word + "\nРусский: " + translated,
                            "Результат перевода",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame,
                            "Введите английское слово!",
                            "Ошибка",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        // Обработчик для кнопки генерации Word документа
        btnGenerateWord.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(frame,
                        "Сгенерировать и открыть Word отчет о словаре?",
                        "Генерация отчета",
                        JOptionPane.YES_NO_OPTION);

                if (choice == JOptionPane.YES_OPTION) {
                    generateWordReport();
                }
            }
        });

        // Обработчик для кнопки очистки
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                txtRussian.setText("");
                txtEnglish.setText("");
                JOptionPane.showMessageDialog(frame,
                        "Поля очищены",
                        "Информация",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Добавляем меню
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem exitItem = new JMenuItem("Выход");
        JMenuItem wordItem = new JMenuItem("Создать Word отчет");

        exitItem.addActionListener(e -> System.exit(0));
        wordItem.addActionListener(e -> generateWordReport());

        fileMenu.add(wordItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
    }

    private static String sendRequest(String paramName, String word) {
        try {
            // Кодируем слово для URL
            String encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8);

            // URL сервлета перевода
            String url = "http://localhost:8080/servlet_war_exploded/HelloServlet?" + paramName + "=" + encodedWord;

            System.out.println("Отправка запроса перевода: " + url);

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("Ответ сервера: " + response.statusCode() + " - " + response.body());

            if (response.statusCode() == 200) {
                return response.body();
            } else {
                return "Ошибка сервера: " + response.statusCode();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "Ошибка подключения: " + ex.getMessage();
        }
    }

    private static void generateWordReport() {
        new Thread(() -> {
            try {
                String url = "http://localhost:8080/servlet_war_exploded/WordServlet";

                System.out.println("Запрос генерации Word документа: " + url);

                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(30))
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<InputStream> response = client.send(request,
                        HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() == 200) {
                    // Сохраняем файл и открываем его
                    String filePath = saveAndOpenWordFile(response.body());

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame,
                                "Word документ успешно создан и открыт!\nФайл: " + filePath,
                                "Успех",
                                JOptionPane.INFORMATION_MESSAGE);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(frame,
                                "Ошибка при создании документа: " + response.statusCode(),
                                "Ошибка",
                                JOptionPane.ERROR_MESSAGE);
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame,
                            "Ошибка: " + ex.getMessage(),
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();

        // Показываем сообщение о процессе
        JOptionPane.showMessageDialog(frame,
                "Идет генерация Word документа...",
                "Пожалуйста подождите",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static String saveAndOpenWordFile(InputStream inputStream) throws Exception {
        // Создаем имя файла с timestamp
        String fileName = "dictionary_report_" + System.currentTimeMillis() + ".docx";

        // Сохраняем в папку Downloads или временную папку
        String downloadsPath = System.getProperty("user.home") + "/Downloads";
        Path downloadsDir = Paths.get(downloadsPath);

        // Если папка Downloads не существует, используем временную папку
        if (!Files.exists(downloadsDir)) {
            downloadsPath = System.getProperty("java.io.tmpdir");
        }

        Path filePath = Paths.get(downloadsPath, fileName);

        // Копируем файл
        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Файл сохранен: " + filePath.toString());

        // Пытаемся автоматически открыть файл
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(filePath.toFile());
                    System.out.println("Файл открыт автоматически");
                }
            }
        } catch (Exception e) {
            System.out.println("Не удалось открыть файл автоматически: " + e.getMessage());
            System.out.println("Файл сохранен по пути: " + filePath.toString());
        }

        return filePath.toString();
    }
}