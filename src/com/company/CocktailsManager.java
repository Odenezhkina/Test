package com.company;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Контрольная 2
// Денежкина Ольга 11-101
// 1 вариант: Коктели
public class CocktailsManager {
    private static final String SAVING_PATH = "/Users/olga/desktop/";
    private static final String PATH_TO_PROPERTIES = "/Users/olga/IdeaProjects/Test/src/com/company/prop.properties";
    private static final String KEY_TO_URL = "cocktailURL";
    private String cocktailName;

    public CocktailsManager(String cocktailName) throws IOException {
        if (cocktailName.length() < 1 || stringContainsChars(cocktailName)) {
            throw new NullPointerException("Invalid cocktail name");
        }
        File file = new File(PATH_TO_PROPERTIES);
        try {
            Properties properties = new Properties();
            properties.load(new FileReader(file));
            String query = properties.getProperty(KEY_TO_URL) + cocktailName;
            execute(query);
        } catch (IOException e) {
            throw new IOException("Error while loading properties");
        }
    }

    private void execute(String query) throws IOException {
        try {
            URL url = new URL(query);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder sb = new StringBuilder();
            in.lines().forEach(l -> sb.append(l));
            String response = sb.toString();
            downloadImage(response);
            writeIniFile(response);
        } catch (MalformedURLException e) {
            throw new IOException("Invalid url");
        }
    }

    private void downloadImage(String response) throws IOException {
        String imageUrl = findMatch("\"strDrinkThumb\":\".+?\"", response)
                .replaceAll("\\\\", "").replaceAll("\"", "");
        imageUrl = imageUrl.split(":")[1] + ":" + imageUrl.split(":")[2];
        cocktailName = findMatch("\"strDrink\":\".+?\"", response).replaceAll("\"", "").split(":")[1];
        String extension = imageUrl.split("\\.")[imageUrl.split("\\.").length - 1];
        File file = new File(SAVING_PATH + cocktailName + "." + extension);
        try (BufferedInputStream in = new BufferedInputStream(new URL(imageUrl).openStream());
             BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))
        ) {
            int b = in.read();
            while (b != -1) {
                out.write(b);
                b = in.read();
            }
            out.flush();
        } catch (IOException e) {
            throw new IOException("Invalid url to download picture");
        }
    }

    private String findMatch(String regex, String body) {
        String str = "";
        Matcher m = Pattern.compile(regex).matcher(body);
        if (m.find()) {
            str = m.group();
        }
        return str;
    }

    private List<String> findAllMatches(String regex, String body) {
        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern.compile(regex).matcher(body);
        while (m.find()) {
            allMatches.add(m.group().replaceAll("\"", "").split(":")[1]);
        }
        return allMatches;
    }

    private void writeIniFile(String response) throws IOException {
        List<String> ingList = findAllMatches("\"strIngredient[0-9][0-9]?\":\".+?\"", response);
        List<String> mesList = findAllMatches("\"strMeasure[0-9][0-9]?\":\".+?\"", response);
        File file = new File(SAVING_PATH + cocktailName + ".ini");
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
            for (int i = 0; i < ingList.size(); i++) {
                out.write(ingList.get(i) + " = " + ((mesList.size() > i) ? mesList.get(i) : "null") + "\n");
            }
            out.flush();
        } catch (IOException e) {
            throw new IOException("Error while writing ini file");
        }
    }

    private boolean stringContainsChars(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (Character.isDigit(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
