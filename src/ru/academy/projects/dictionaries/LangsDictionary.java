package ru.academy.projects.dictionaries;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class LangsDictionary {
    private static final String SAVE_FILE = "dictionaries.properties";
    private final Map<String, Map<String, WordTranslationField>> dictionaries = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);
    private String currentLangPair = "en-ru";

    public LangsDictionary() {
        load();
        ensureLangPair(currentLangPair);
    }

    public static void main(String[] args) {
        new LangsDictionary().run();
    }

    private Map<String, WordTranslationField> getCurrentDict() {
        return dictionaries.computeIfAbsent(currentLangPair, k -> new TreeMap<>());
    }

    private void run() {
        System.out.println("Многоязычный словарь");
        System.out.println("Команды: -lang <from> <to>, -a, -r, -d, -list, -top, -low, -paste, -pairs, -q");
        update();

        String input;
        while (!(input = scanner.nextLine().trim()).equals("-q")) {
            if (input.isEmpty()) continue;
            if (input.startsWith("-")) {
                selectCmd(input);
            } else {
                lookup(input);
            }
            update();
        }

        save();
        System.out.println("Данные сохранены.");
    }

    private void update() {
        System.out.print("[" + currentLangPair + "] @ ");
    }

    private void ensureLangPair(String pair) {
        dictionaries.putIfAbsent(pair, new TreeMap<>());
    }

    private void selectCmd(String strLine) {
        if (strLine.startsWith("-a ")) {
            // специальная обработка для команды -a
            String[] parts = strLine.split("\\s+", 3);
            if (parts.length < 3) {
                System.out.println("Используйте: -a слово перевод1,перевод2,....");
                return;
            }
            add(parts);//(parts[1], parts[2]);
            return;
        }
        String[] parts = strLine.split("\\s+", 4);
        String cmd = parts[0];
        try {
            switch (cmd) {
                case "-lang" -> setLanguagePair(parts);
                //case "-a" -> add(parts);
                case "-r" -> replace(parts);
                case "-d" -> delete(parts);
                case "-list" -> listAll();
                case "-top" -> showTop(10);
                case "-low" -> showLow(10);
                case "-paste" -> pasteBlock();
                case "-pairs" -> showPairs();
                case "-h", "-help" -> showHelp();
                default -> System.out.println("Неизвестная команда: " + cmd);
            }
        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
        }
    }

    private void setLanguagePair(String[] parts) {
        if (parts.length < 3) {
            System.out.println("Используйте: -lang <from> <to>");
            return;
        }
        String from = parts[1].toLowerCase();
        String to = parts[2].toLowerCase();
        if (from.equals(to)) {
            System.out.println("Языки должны отличаться.");
            return;
        }
        currentLangPair = from + "-" + to;
        ensureLangPair(currentLangPair);
        System.out.println("Текущее направление перевода: " + currentLangPair);
    }

    private void showHelp() {
        System.out.println("Справка по командам:");
        System.out.println("  слово               — найти перевод слова");
        System.out.println("  -a слово перевод1, перевод2, ... — добавить слово и перевод(ы)");
        System.out.println("  -lang <from> <to>  — сменить направление перевода (напр.: en ru)");
        System.out.println("  -list              — показать все слова в текущем словаре");
        System.out.println("  -top               — показать 10 самых запрашиваемых слов");
        System.out.println("  -low               — показать 10 самых не востребованных слов");
        System.out.println("  -pairs             — показать доступные языковые пары");
        System.out.println("  -h, -help          — показать эту справку");
        System.out.println("  -q                 — выйти и сохранить словарь");
    }

    private void add(String[] partsLine) {

        if (partsLine.length < 3) {
            System.out.println("Используйте: -a слово перевод1,перевод2,...");
            return;
        }
        String word = partsLine[1].toLowerCase();
        String trInput = partsLine[2];
        WordTranslationField entry = getCurrentDict().computeIfAbsent(word, k -> new WordTranslationField());
        for (String tr : trInput.split(",")) {
            entry.addTranslation(tr);
        }
        System.out.printf("+%s: [%s]%n", word, String.join(", ", entry.translations));
    }

    private void replace(String[] partsLine) {
        if (partsLine.length < 3) {
            System.out.println("Используйте: -r слово новый_перевод");
            return;
        }
        String word = partsLine[1].toLowerCase();
        String trInput = partsLine[2];
        WordTranslationField entry = getCurrentDict().get(word);
        if (entry == null) {
            System.out.println("Слово отсутствует. Используйте -a.");
            return;
        }
        entry.translations.clear();
        for (String tr : trInput.split(",")) {
            entry.addTranslation(tr);
        }
        System.out.printf("%s → [%s]%n", word, String.join(", ", entry.translations));
    }

    private void lookup(String word) {
        Map<String, WordTranslationField> dict = getCurrentDict();
        WordTranslationField el = dict.get(word.toLowerCase());
        if (el != null) {
            el.requestsCount++;
            System.out.println(word + " -> " + el);
        } else {
            System.out.println("Слово " + word + " не найдено.");
        }
    }

    private void delete(String[] partsLine) {
        if (partsLine.length < 2) {
            System.out.println("Используйте: -d слово [перевод|all]");
            return;
        }
        String word = partsLine[1].toLowerCase();
        Map<String, WordTranslationField> dict = getCurrentDict();
        if (partsLine.length == 2) {
            if (dict.remove(word) != null) {
                System.out.println("Слово \"" + word + "\" удалено из " + currentLangPair);
            } else {
                System.out.println("Не найдено в " + currentLangPair);
            }
        } else {
            String arg = partsLine[2].trim();
            WordTranslationField entry = dict.get(word);
            if (entry == null) {
                System.out.println("Слово не найдено.");
                return;
            }
            if ("all".equalsIgnoreCase(arg)) {
                entry.translations.clear();
                System.out.println("Переводы у \"" + word + "\" очищены.");
            } else {
                if (entry.removeTranslation(arg)) {
                    if (entry.translations.isEmpty()) dict.remove(word);
                    System.out.println("Перевод удалён.");
                } else {
                    System.out.println("Перевод не найден.");
                }
            }
        }
    }

    private void listAll() {
        Map<String, WordTranslationField> dict = getCurrentDict();
        if (dict.isEmpty()) {
            System.out.println("Словарь " + currentLangPair + " пуст");
            return;
        }
        System.out.println("Словарь " + currentLangPair + " (" + dict.size() + " слов):");
        dict.forEach((wd, tr) ->
                System.out.printf("  %s [%d] → [%s]%n", wd, tr.requestsCount, String.join(", ", tr.translations))
        );
    }

    private void showTop(int n) {
        showRanking("ТОП-" + n, true, n);
    }

    private void showLow(int n) {
        showRanking("НИЗ-" + n + " (>=1)", false, n);
    }

    private void showRanking(String title, boolean top, int n) {
        var entries = getCurrentDict().entrySet().stream()
                .filter(e -> e.getValue().requestsCount > 0)
                .sorted(Map.Entry.<String, WordTranslationField>comparingByValue(
                        Comparator.comparingInt(e -> top ? -e.requestsCount : e.requestsCount)
                ))
                .limit(n)
                .collect(Collectors.toList());

        if (entries.isEmpty()) {
            System.out.println("Нет обращений в " + currentLangPair);
            return;
        }
        System.out.println(" " + title + " в " + currentLangPair + ":");
        for (int i = 0; i < entries.size(); i++) {
            var en = entries.get(i);
            System.out.printf("  %2d. [%3d×] %s -> [%s]%n",
                    i + 1, en.getValue().requestsCount, en.getKey(),
                    String.join(", ", en.getValue().translations));
        }
    }

    private void showPairs() {
        System.out.println("Доступные направления:");
        dictionaries.keySet().stream().sorted().forEach(p -> {
            int size = dictionaries.get(p).size();
            String current = p.equals(currentLangPair) ? " <- текущее" : "";
            System.out.println("  " + p + " (" + size + " слов)" + current);
        });
    }

    private void save() {
        Properties properties = new Properties();
        for (String pair : dictionaries.keySet()) {
            for (String word : dictionaries.get(pair).keySet()) {
                WordTranslationField el = dictionaries.get(pair).get(word);
                properties.setProperty(pair + "." + word + ".tr", String.join(", ", el.translations));
                properties.setProperty(pair + "." + word + ".reqCnt", String.valueOf(el.requestsCount));
            }
        }
        try (var out = new FileOutputStream(SAVE_FILE)) {
            properties.store(out, "Auto-saved dictionary");
        } catch (IOException ex) {
            System.out.println("Save failed");
        }
    }

    private void load() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return;

        Properties properties = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            properties.load(in);
        } catch (IOException ex) {
            System.err.println("Ошибка загрузки " + SAVE_FILE + ": " + ex.getMessage());
            return;
        }

        for (String key : properties.stringPropertyNames()) {
            if (key.endsWith(".tr")) {
                String[] parts = key.split("\\.");
                if (parts.length >= 3) {
                    String pair = parts[0];
                    String word = parts[1];

                    Map<String, WordTranslationField> dict = dictionaries.computeIfAbsent(pair, k -> new TreeMap<>());
                    WordTranslationField field = dict.computeIfAbsent(word, k -> new WordTranslationField());

                    String translationStr = properties.getProperty(key, "");
                    field.translations.clear();
                    for (String tr : translationStr.split(",")) {
                        field.addTranslation(tr);
                    }

                    String countKey = key.replace(".tr", ".reqCnt");
                    String countStr = properties.getProperty(countKey, "0");
                    try {
                        field.requestsCount = Integer.parseInt(countStr.trim());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
    }

    private void pasteBlock() {
        System.out.println("Вставьте блок (пустую строку для завершения):");
        StringBuilder sb = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).isEmpty()) {
            sb.append(line).append("\n");
        }
        int count = 0;
        for (String l : sb.toString().split("\n")) {
            l = l.trim();
            if (l.isEmpty() || l.startsWith("#")) continue;
            String[] kv = l.contains("=") ? l.split("=", 2) : l.split("\\s+", 2);
            if (kv.length < 2) continue;
            String word = kv[0].trim().toLowerCase();
            String trs = kv[1].trim();
            WordTranslationField e = getCurrentDict().computeIfAbsent(word, k -> new WordTranslationField());
            for (String tr : trs.split(",")) {
                e.addTranslation(tr);
            }
            count++;
        }
        System.out.println("Добавлено " + count + " слов в " + currentLangPair);
    }
}