package com.schoolproject.javaav.swing;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class GestionDeNoteUI {
    private static final String API_URL = "http://localhost:8080/api/etudiants";
    private JFrame frame;
    private JTable table;
    private DefaultTableModel model;
    private JTextField numEtField, nomField, prenomField, moyenneField;
    private int selectedId = -1;
    private JButton addButton, updateButton, deleteButton, clearButton;
    private JLabel averageLabel, minLabel, maxLabel;
    private JPanel chartPanel;
    private JTabbedPane chartTabbedPane;

    // Loading components
    private JDialog loadingDialog;
    private JProgressBar progressBar;
    private JLabel statusLabel;

    public GestionDeNoteUI() {
        // Create and show the main frame immediately
        frame = new JFrame("Gestion des Étudiants");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 650);
        frame.setLayout(new BorderLayout(10, 10));

        // Show loading dialog
        createAndShowLoadingDialog();

        // Initialize the application in a background thread
        SwingWorker<Void, String> worker = new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                // Update progress status
                publish("Initialisation de l'interface...");
                Thread.sleep(500); // Small delay to show the message

                initializeUI();

                publish("Chargement des données...");
                Thread.sleep(500); // Small delay to show the message

                loadStudents();

                publish("Préparation des graphiques...");
                Thread.sleep(5000); // Small delay to show the message

                return null;
            }

            @Override
            protected void process(java.util.List<String> chunks) {
                // Update the status label with the latest status message
                if (!chunks.isEmpty()) {
                    statusLabel.setText(chunks.get(chunks.size() - 1));
                    progressBar.setValue(progressBar.getValue() + 10);
                }
            }

            @Override
            protected void done() {
                // Hide the loading dialog and show the main frame
                loadingDialog.dispose();
                frame.setVisible(true);
            }
        };

        worker.execute();
    }

    private void createAndShowLoadingDialog() {
        loadingDialog = new JDialog(frame, "Chargement...", true);
        loadingDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        loadingDialog.setSize(300, 150);
        loadingDialog.setLayout(new BorderLayout(10, 10));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        statusLabel = new JLabel("Démarrage de l'application...");

        progressBar = new JProgressBar(0, 100);
        progressBar.setIndeterminate(false);
        progressBar.setValue(10);
        progressBar.setStringPainted(true);

        JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.informationIcon"));

        panel.add(iconLabel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        centerPanel.add(statusLabel);
        centerPanel.add(progressBar);

        panel.add(centerPanel, BorderLayout.CENTER);
        loadingDialog.add(panel);

        // Center the dialog on screen
        loadingDialog.setLocationRelativeTo(null);

        // Show the dialog in a separate thread
        new Thread(() -> loadingDialog.setVisible(true)).start();
    }

    private void initializeUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        frame.add(mainPanel);

        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(new TitledBorder("Informations de l'étudiant"));
        numEtField = createTextField("Numéro Étudiant:", formPanel);
        nomField = createTextField("Nom:", formPanel);
        prenomField = createTextField("Prénom:", formPanel);
        moyenneField = createTextField("Moyenne:", formPanel);
        mainPanel.add(formPanel, BorderLayout.NORTH);

        // Table Panel
        model = new DefaultTableModel(new String[]{"ID", "NumEt", "Nom", "Prénom", "Moyenne", "Observation"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);


        // Custom renderer for the Observation column
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String observation = (String) value;
                if ("Admis".equals(observation)) {
                    c.setForeground(new Color(0, 128, 0)); // Dark Green
                } else if ("Redoublant".equals(observation)) {
                    c.setForeground(new Color(255, 140, 0)); // Orange
                } else if ("Exclus".equals(observation)) {
                    c.setForeground(Color.RED);
                }
                return c;
            }
        });

        // Add listener to table selection to populate form fields
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row >= 0) {
                    selectedId = (int) table.getValueAt(row, 0);
                    numEtField.setText((String) table.getValueAt(row, 1));
                    nomField.setText((String) table.getValueAt(row, 2));
                    prenomField.setText((String) table.getValueAt(row, 3));
                    moyenneField.setText(String.valueOf(table.getValueAt(row, 4)));

                    // Enable update and delete buttons
                    updateButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                }
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setBorder(new TitledBorder("Liste des étudiants"));

        // Stats Panel
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        statsPanel.setBorder(new TitledBorder("Statistiques de la classe"));
        averageLabel = new JLabel("Moyenne de classe: --");
        minLabel = new JLabel("Note minimale: --");
        maxLabel = new JLabel("Note maximale: --");

        statsPanel.add(averageLabel);
        statsPanel.add(minLabel);
        statsPanel.add(maxLabel);

        // Create a panel to hold table and stats
        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.add(tableScrollPane, BorderLayout.CENTER);
        dataPanel.add(statsPanel, BorderLayout.SOUTH);

        // Charts Panel
        chartTabbedPane = new JTabbedPane();

        // Create an initial panel to hold charts (will be populated later)
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(new TitledBorder("Visualisation graphique"));

        // Split pane for data and charts
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                dataPanel,
                chartPanel
        );
        splitPane.setResizeWeight(0.6); // Give 60% space to the data panel

        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(1, 5, 5, 5));
        addButton = createButton("Ajouter", Color.GREEN, e -> addStudent());
        updateButton = createButton("Modifier", Color.ORANGE, e -> updateStudent());
        deleteButton = createButton("Supprimer", Color.RED, e -> deleteStudent());
        clearButton = createButton("Effacer", Color.BLUE, e -> clearForm());
        JButton refreshButton = createButton("Rafraîchir", new Color(0, 139, 139), e -> refreshData());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(refreshButton);

        // Initially disable update and delete buttons
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
    }

    private void refreshData() {
        // Show loading dialog for refresh operation
        JDialog refreshDialog = new JDialog(frame, "Rafraîchissement...", true);
        refreshDialog.setSize(300, 100);
        refreshDialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel label = new JLabel("Chargement des données...");
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);

        panel.add(label, BorderLayout.NORTH);
        panel.add(bar, BorderLayout.CENTER);
        refreshDialog.add(panel);
        refreshDialog.setLocationRelativeTo(frame);

        // Use SwingWorker to load data in background
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadStudents();
                return null;
            }

            @Override
            protected void done() {
                refreshDialog.dispose();
            }
        };

        worker.execute();
        refreshDialog.setVisible(true);
    }

    private JTextField createTextField(String label, JPanel panel) {
        panel.add(new JLabel(label));
        JTextField textField = new JTextField();
        panel.add(textField);
        return textField;
    }

    private JButton createButton(String text, Color color, ActionListener action) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.addActionListener(action);
        return button;
    }

    private void loadStudents() {
        try {
            model.setRowCount(0);
            HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner scanner = new Scanner(conn.getInputStream());
                String response = scanner.useDelimiter("\\A").next();
                scanner.close();

                JSONArray students = new JSONArray(response);

                double sumMoyenne = 0;
                double minMoyenne = Double.MAX_VALUE;
                double maxMoyenne = Double.MIN_VALUE;
                int totalStudents = students.length();

                for (int i = 0; i < students.length(); i++) {
                    JSONObject student = students.getJSONObject(i);
                    double moyenne = student.getDouble("moyenne");

                    // Calculate statistics
                    sumMoyenne += moyenne;
                    minMoyenne = Math.min(minMoyenne, moyenne);
                    maxMoyenne = Math.max(maxMoyenne, moyenne);

                    String observation = getObservation(moyenne);

                    model.addRow(new Object[]{
                            student.getInt("id"),
                            student.getString("numEt"),
                            student.getString("nom"),
                            student.getString("prenom"),
                            moyenne,
                            observation
                    });
                }

                if (totalStudents > 0) {
                    double avgMoyenne = sumMoyenne / totalStudents;
                    updateStats(avgMoyenne, minMoyenne, maxMoyenne);
                    createCharts(avgMoyenne, minMoyenne, maxMoyenne);
                } else {
                    updateStats(0, 0, 0);
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Erreur du serveur: " + responseCode,
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Erreur lors du chargement des étudiants: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getObservation(double moyenne) {
        if (moyenne >= 10) {
            return "Admis";
        } else if (moyenne >= 5) {
            return "Redoublant";
        } else {
            return "Exclus";
        }
    }

    private void updateStats(double avg, double min, double max) {
        String format = "%.2f";
        averageLabel.setText("Moyenne de classe: " + String.format(format, avg));
        minLabel.setText("Note minimale: " + String.format(format, min));
        maxLabel.setText("Note maximale: " + String.format(format, max));
    }

    private void createCharts(double avg, double min, double max) {
        // Clear previous charts
        chartPanel.removeAll();
        chartTabbedPane.removeAll();

        // Create histogram
        DefaultCategoryDataset histogramDataset = new DefaultCategoryDataset();
        histogramDataset.addValue(min, "Notes", "Minimum");
        histogramDataset.addValue(avg, "Notes", "Moyenne");
        histogramDataset.addValue(max, "Notes", "Maximum");

        JFreeChart histogram = ChartFactory.createBarChart(
                "Statistiques des notes",
                "Mesure",
                "Valeur",
                histogramDataset,
                PlotOrientation.VERTICAL,
                true, true, false);
        ChartPanel histogramPanel = new ChartPanel(histogram);

        // Create pie chart - count students by category
        DefaultPieDataset pieDataset = new DefaultPieDataset();
        int admisCount = 0, redoublantCount = 0, exclusCount = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            String observation = (String) model.getValueAt(i, 5);
            if ("Admis".equals(observation)) {
                admisCount++;
            } else if ("Redoublant".equals(observation)) {
                redoublantCount++;
            } else if ("Exclus".equals(observation)) {
                exclusCount++;
            }
        }

        pieDataset.setValue("Admis", admisCount);
        pieDataset.setValue("Redoublant", redoublantCount);
        pieDataset.setValue("Exclus", exclusCount);

        JFreeChart pieChart = ChartFactory.createPieChart(
                "Répartition des étudiants par catégorie",
                pieDataset,
                true, true, false);

        // Set colors for the pie chart
        PiePlot plot = (PiePlot) pieChart.getPlot();
        plot.setSectionPaint("Admis", new Color(0, 128, 0));
        plot.setSectionPaint("Redoublant", new Color(255, 140, 0));
        plot.setSectionPaint("Exclus", Color.RED);

        ChartPanel pieChartPanel = new ChartPanel(pieChart);

        // Add charts to tabbed pane
        chartTabbedPane.addTab("Histogramme", histogramPanel);
        chartTabbedPane.addTab("Camembert", pieChartPanel);

        chartPanel.add(chartTabbedPane, BorderLayout.CENTER);
        chartPanel.revalidate();
        chartPanel.repaint();
    }

    private boolean validateForm() {
        if (numEtField.getText().trim().isEmpty() ||
                nomField.getText().trim().isEmpty() ||
                prenomField.getText().trim().isEmpty() ||
                moyenneField.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(frame, "Tous les champs sont obligatoires!",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        try {
            double moyenne = Double.parseDouble(moyenneField.getText());
            if (moyenne < 0 || moyenne > 20) {
                JOptionPane.showMessageDialog(frame, "La moyenne doit être entre 0 et 20!",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "La moyenne doit être un nombre valide!",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void addStudent() {
        if (!validateForm()) {
            return;
        }

        String numEt = numEtField.getText();
        // Check if student number already exists
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 1).equals(numEt)) {
                JOptionPane.showMessageDialog(frame, "Ce numéro étudiant existe déjà!",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Voulez-vous ajouter cet étudiant?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (sendRequest("POST", -1)) {
                JOptionPane.showMessageDialog(frame, "Étudiant ajouté avec succès!",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
            }
        }
    }

    private void updateStudent() {
        if (selectedId == -1) {
            JOptionPane.showMessageDialog(frame, "Sélectionnez un étudiant à modifier.");
            return;
        }

        if (!validateForm()) {
            return;
        }

        // Check if student number already exists (excluding current selection)
        String numEt = numEtField.getText();
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 1).equals(numEt) && (int)model.getValueAt(i, 0) != selectedId) {
                JOptionPane.showMessageDialog(frame, "Ce numéro étudiant existe déjà!",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Voulez-vous modifier cet étudiant?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (sendRequest("PUT", selectedId)) {
                JOptionPane.showMessageDialog(frame, "Étudiant modifié avec succès!",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
            }
        }
    }

    private void deleteStudent() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(frame, "Sélectionnez un étudiant à supprimer.");
            return;
        }

        selectedId = (int) table.getValueAt(row, 0);
        String studentName = table.getValueAt(row, 2) + " " + table.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(frame,
                "Voulez-vous vraiment supprimer l'étudiant " + studentName + "?",
                "Confirmation de suppression", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(API_URL + "/" + selectedId).openConnection();
                conn.setRequestMethod("DELETE");

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) {
                    loadStudents();
                    clearForm();
                    JOptionPane.showMessageDialog(frame, "Étudiant supprimé avec succès!",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(frame, "Erreur lors de la suppression. Code: " + responseCode,
                            "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Erreur lors de la suppression: " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean sendRequest(String method, int id) {
        try {
            URL url = new URL(id == -1 ? API_URL : API_URL + "/" + id);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(method);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JSONObject student = new JSONObject();
            if (id != -1) student.put("id", id);
            student.put("numEt", numEtField.getText());
            student.put("nom", nomField.getText());
            student.put("prenom", prenomField.getText());
            student.put("moyenne", Double.parseDouble(moyenneField.getText()));

            OutputStream os = conn.getOutputStream();
            os.write(student.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK ||
                    responseCode == HttpURLConnection.HTTP_CREATED ||
                    responseCode == HttpURLConnection.HTTP_NO_CONTENT) {

                loadStudents();
                return true;
            } else {
                JOptionPane.showMessageDialog(frame, "Erreur lors de l'envoi des données. Code: " + responseCode,
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Erreur lors de l'envoi des données: " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void clearForm() {
        numEtField.setText("");
        nomField.setText("");
        prenomField.setText("");
        moyenneField.setText("");
        selectedId = -1;
        table.clearSelection();
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
}