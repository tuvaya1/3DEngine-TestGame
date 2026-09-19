package org.tuvaya.launcher;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Louncher extends JFrame {

    private final JTextArea outputArea = new JTextArea();
    private final JScrollPane scrollPane = new JScrollPane(outputArea);

    private Process currentProcess;
    private volatile boolean autoScroll = true;

    public Louncher() {
        setTitle("Launcher");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(500, 400));
        setLocationRelativeTo(null);

        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Форма ──
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        JTextField nameField  = new JTextField(15);
        JTextField ipField    = new JTextField(15);
        JTextField portField  = new JTextField(15);
        JTextField scaleField = new JTextField(15);
        JCheckBox  fullBox    = new JCheckBox("Fullscreen");

        addRow(form, c, 0, "Username:", nameField);
        addRow(form, c, 1, "IP:",       ipField);
        addRow(form, c, 2, "Port:",     portField);
        addRow(form, c, 3, "Scale:",    scaleField);

        c.gridx = 1; c.gridy = 4; c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        form.add(fullBox, c);

        // ── Кнопки Start / Stop ──
        JButton startButton = new JButton("Start");
        JButton stopButton  = new JButton("Stop");
        stopButton.setEnabled(true);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        buttonPanel.add(stopButton);
        buttonPanel.add(startButton);

        c.gridx = 1; c.gridy = 5; c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.EAST;
        form.add(buttonPanel, c);

        add(form, BorderLayout.NORTH);

        // ── Консоль ──
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        outputArea.setBackground(Color.WHITE);
        outputArea.setForeground(Color.BLACK);
        outputArea.setMargin(new Insets(4, 4, 4, 4));

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Output"));

        // Отслеживаем ручную прокрутку: если пользователь ушёл от низа — выключаем автопрокрутку
        scrollPane.getVerticalScrollBar().addAdjustmentListener(ev -> {
            JScrollBar bar = (JScrollBar) ev.getAdjustable();
            int extent = bar.getModel().getExtent();
            boolean atBottom = (bar.getValue() + extent) >= bar.getMaximum();
            // Если пользователь прокрутил вверх — отключаем "прилипание"
            if (!atBottom && ev.getValueIsAdjusting()) {
                autoScroll = false;
            }
        });

        // ── Нижняя панель: кнопка "Вниз" + чекбокс автопрокрутки ──
        JButton scrollDownButton = new JButton("↓ К низу");
        scrollDownButton.setToolTipText("Прилипнуть к низу и возобновить автопрокрутку");
        scrollDownButton.addActionListener(e -> {
            autoScroll = true;
            scrollToBottom();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        bottomPanel.add(scrollDownButton);

        JPanel centerWrapper = new JPanel(new BorderLayout(0, 4));
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        centerWrapper.add(bottomPanel, BorderLayout.SOUTH);
        add(centerWrapper, BorderLayout.CENTER);

        // ── Обработчики ──
        startButton.addActionListener(e -> startProcess(
                nameField.getText().trim(),
                ipField.getText().trim(),
                portField.getText().trim(),
                scaleField.getText().trim(),
                fullBox.isSelected()
        ));

        stopButton.addActionListener(e -> stopProcess());

        // При закрытии окна убиваем процесс
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                stopProcess();
            }
        });

        pack();
    }

    /** Добавляет строку "метка — поле" в GridBagLayout. */
    private void addRow(JPanel p, GridBagConstraints c, int row,
                        String label, JComponent field) {
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;
        p.add(new JLabel(label), c);

        c.gridx = 1; c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(field, c);
    }

    private void startProcess(String name, String ip, String portStr,
                              String scaleStr, boolean full) {
        if (currentProcess != null && currentProcess.isAlive()) {
            append("Процесс уже запущен. Нажмите Stop.\n");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException ex) {
            append("Ошибка: порт должен быть числом\n");
            return;
        }

        outputArea.setText("");
        autoScroll = true;
        append("Запуск: java -jar Untitled.jar name=" + name + " ip=" + ip +
                " port=" + port + " full=" + full + " scale=" + scaleStr + "\n\n");

        ProcessBuilder pb = new ProcessBuilder(
                "java", "-jar", "Untitled.jar",
                "name=" + name,
                "ip=" + ip,
                "port=" + port,
                "full=" + full,
                "scale=" + scaleStr
        );
        pb.redirectErrorStream(true);

        try {
            currentProcess = pb.start();

            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(currentProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        append(line + "\n");
                    }
                    int code = currentProcess.waitFor();
                    append("\n--- Процесс завершён, код: " + code + " ---\n");
                } catch (Exception ex) {
                    append("Ошибка чтения: " + ex.getMessage() + "\n");
                }
            }).start();

        } catch (Exception ex) {
            append("Не удалось запустить процесс: " + ex.getMessage() + "\n");
        }
    }

    private void stopProcess() {
        if (currentProcess == null || !currentProcess.isAlive()) {
            append("Процесс не запущен.\n");
            return;
        }
        append("\n--- Остановка процесса... ---\n");
        currentProcess.destroy(); // мягкое завершение (SIGTERM / TerminateProcess)

        // Если через 2 секунды не завершился — убиваем жёстко
        new Thread(() -> {
            try {
                if (!currentProcess.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    currentProcess.destroyForcibly();
                    append("--- Процесс убит принудительно ---\n");
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }

    private void append(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            if (autoScroll) {
                scrollToBottom();
            }
        });
    }

    private void scrollToBottom() {
        JScrollBar bar = scrollPane.getVerticalScrollBar();
        bar.setValue(bar.getMaximum());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Louncher().setVisible(true));
    }
}