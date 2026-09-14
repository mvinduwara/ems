package com.retailhr.ems.util;

import javafx.scene.Scene;

import java.util.Objects;

public class ThemeManager {

    private static final String LIGHT_CSS = "css/app-light.css";
    private static final String DARK_CSS = "css/app-dark.css";

    private static boolean darkMode = false;

    private ThemeManager() {
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void applyCurrentTheme(Scene scene) {
        scene.getStylesheets().clear();
        String cssPath = darkMode ? DARK_CSS : LIGHT_CSS;
        scene.getStylesheets().add(
                Objects.requireNonNull(ThemeManager.class.getResource("/com/retailhr/ems/" + cssPath)).toExternalForm()
        );
    }

    public static void toggleTheme(Scene scene) {
        darkMode = !darkMode;
        applyCurrentTheme(scene);
    }
}