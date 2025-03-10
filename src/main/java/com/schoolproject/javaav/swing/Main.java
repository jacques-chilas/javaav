package com.schoolproject.javaav.swing;

import javax.swing.*;

public class Main {
    // Ajout de la méthode main pour lancer l'application Swing
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GestionDeNoteUI::new);
    }
}
