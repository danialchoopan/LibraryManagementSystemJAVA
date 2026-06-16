package com.library.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class MessageManager {
    private static MessageManager instance;
    private Properties properties;
    private Language currentLanguage;

    private MessageManager() {
        currentLanguage = Language.FA; // Default to Farsi
        loadProperties(currentLanguage);
    }

    public static synchronized MessageManager getInstance() {
        if (instance == null) {
            instance = new MessageManager();
        }
        return instance;
    }

    private void loadProperties(Language language) {
        properties = new Properties();
        String fileName = "/i18n/messages_" + language.getCode() + ".properties";
        try (InputStream input = getClass().getResourceAsStream(fileName)) {
            if (input == null) {
                System.err.println("Could not find messages file: " + fileName);
                return;
            }
            properties.load(new InputStreamReader(input, StandardCharsets.UTF_8));
        } catch (IOException e) {
            System.err.println("Error loading messages: " + e.getMessage());
        }
    }

    public String getMessage(String key) {
        return properties.getProperty(key, key);
    }

    public String getMessage(String key, Object... args) {
        String message = getMessage(key);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return message;
    }

    public void setLanguage(Language language) {
        this.currentLanguage = language;
        loadProperties(language);
    }

    public Language getCurrentLanguage() {
        return currentLanguage;
    }
}