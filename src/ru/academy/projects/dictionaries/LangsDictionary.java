package ru.academy.projects.dictionaries;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyPair;
import java.util.*;

//import ru.academy.projects.dictionaries.WordTranslationField;

public class LangsDictionary {
    // private final Map<String, WordTranslationField> dict = new TreeMap<>();
    private final Map<String, Map<String, WordTranslationField>> dictionaries = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);
    private String currentLangPair = "en-ru";
    private static final String SAVE_FILE = "dictionaries.properties";

    static void main(String[] args) {
        new LangsDictionary().run();
    }

    private Map<String, WordTranslationField> getCurrentDict(){
        return dictionaries.computeIfAbsent(currentLangPair, k -> new TreeMap<>());
    }

    public LangsDictionary(){
        load();
    }
    private void run(){
        System.out.println("Словарь");
        //System.out.println("Команды: -a слово перевод | слово | -list | -top | -q");
       showHelp();

        String input;

        while (!(input = scanner.nextLine().trim()).equals("-q")) {


            if (input.startsWith("-lang ")) {
                String[] parts = input.split("\\s+");
                if (parts.length >=3){
                    setLanguage(parts[1], parts[2]);
                } else {
                    System.out.println("Формат: -lang <from> <to>");
                }
            } else if (input.equals("-lang")){
                System.out.println("Текущее направление перевода: " + currentLangPair);
            }
            else if (input.startsWith("-a")) {
                add(input);
            } else if (input.equals("-list")) {
                list();
            } else if (input.equals("-top")){
                showTop();
            } else if (input.equals("-pairs")) {
                String[] parts = input.split("\\s+",3);
                if(parts.length==1){
                    System.out.println("Направления: " + dictionaries.keySet());
                }else if (parts.length >= 3){
                    String from = parts[1];
                    String to = parts[2];
                    String targetPair = from + "-" + to;
                    if(dictionaries.containsKey(targetPair)){
                        currentLangPair = targetPair;
                        System.out.println("Установлено направление перевода: " + currentLangPair);
                    } else {
                        System.out.println("Ошибка: направление '" + targetPair + "' не существует. Доступные: " + dictionaries.keySet());
                    }
                } else {
                    System.out.println("Формат: -pairs [<from> <to>]");
                    System.out.println(" -pairs       - показать список");
                    System.out.println(" -pairs en ru - переключиться на en-ru");
                }

            }else if (input.equals("-h") || input.equals("-help")){
                showHelp();
            } else if (!input.isEmpty()){
                lookup(input);
            }
        }
        save();
        System.out.println("Пока!");
    }
    private void showHelp(){
        System.out.println("Справка по командам:");
        System.out.println("  слово               — найти перевод слова");
        System.out.println("  -a слово перевод1, перевод2, ... — добавить слово и перевод(ы)");
        System.out.println("  -lang <from> <to>  — сменить направление перевода (напр.: en ru)");
        System.out.println("  -list              — показать все слова в текущем словаре");
        System.out.println("  -top               — показать 3 самых запрашиваемых слова");
        System.out.println("  -lang              — показать текущее направление");
        System.out.println("  -pairs             — показать доступные языковые пары");
        System.out.println("  -lang <from> <to>  — переключиться на пару (если существует)");
        System.out.println("  -h, -help          — показать эту справку");
        System.out.println("  -q                 — выйти и сохранить словарь");
    }
    private void add(String cmd){
//        String[] parts = cmd.substring(3).split("\\s+", 2);
//        if (parts.length < 2){
//            System.out.println("Формат: -a слово перевод1, перевод2, ...");
//            return;
//        }
//        String word = parts[0].toLowerCase();
//        String[] translations = parts[1].split(",");
        // разбиваем всю команду и пропускаем первый аргумент ("-a")
        String[] tokens = cmd.trim().split("\\s+", 3); // макс. 3 части: "-a", слово, переводы
        if(tokens.length < 3 || !tokens[0].equals("-a")){
            System.out.println("Формат: -a слово перевод1, перевод2, ....");
            return;
        }
            String word = tokens[1].toLowerCase();
        String[] translations = tokens[2].split(",");

        Map<String, WordTranslationField> dict = getCurrentDict();
        WordTranslationField el = dict.computeIfAbsent(word, k -> new WordTranslationField());
        for (String tr : translations){
            el.addTranslation(tr.trim());
        }
        System.out.println("Добавлено: " + word + " -> " + el);
    }

    private void lookup(String word){
        Map<String, WordTranslationField> dict = getCurrentDict();
        WordTranslationField el = dict.get(word.toLowerCase());
        if(el != null){
            el.requestsCount++;
            System.out.println(word + " -> " + el);
        } else {
            System.out.println("Слово " + word + " не найдено.");
        }
    }
    private  void list(){
        Map<String, WordTranslationField> dict = getCurrentDict();
        if(dict.isEmpty()){
            System.out.println("Словарь пуст");
            return;
        }
        System.out.println("Словарь " + currentLangPair + ":");
        dict.forEach((wd, tr) -> System.out.println(wd + " -> " + tr));
    }

    private void showTop(){
        Map<String, WordTranslationField> dict = getCurrentDict();
        dict.entrySet().stream()
                .filter(el -> el.getValue().requestsCount > 0)
                .sorted(Map.Entry.<String, WordTranslationField>comparingByValue())
                .limit(3)
                .forEach(el -> System.out.println(el.getKey() + " -> " + el.getValue()));
    }

    private void setLanguage(String from, String to) {
        currentLangPair = from + "-" + to;
        //dictionaries.putIfAbsent(currentLangPair, new TreeMap<>());
        getCurrentDict();
        System.out.println("Направление перевода " + currentLangPair);
    }

    private  void showPairs(){
        if(dictionaries.isEmpty()){
            System.out.println("Нет языковых пар");
        } else {
            System.out.println("Доступные направления перевода: " + new ArrayList<>(dictionaries.keySet()));
        }
    }

    private void save(){
        Properties properties = new Properties();
        for(String pair : dictionaries.keySet()) {
            for(String word : dictionaries.get(pair).keySet()) {
                WordTranslationField el = dictionaries.get(pair).get(word);
                properties.setProperty(pair + "." + word + ".tr", String.join(", ", el.translations));
                properties.setProperty(pair + "." + word + ".reqCnt", String.valueOf(el.requestsCount));
            }
        }
        try (var out = new FileOutputStream(SAVE_FILE)){
            properties.store(out, "Auto-saved dictionary");
        } catch (IOException ex){
            System.out.println("Save failed");
        }
    }

    private void load() {
        File file = new File(SAVE_FILE);
        if (!file.exists())  return;

        Properties properties = new Properties();
        try (var in = new FileInputStream(file)){
            properties.load(in);
        } catch (IOException ex){
            return;
        }

        for (String key : properties.stringPropertyNames()){
            if (key.endsWith(".tr")){
                // рзабор ключа: en-es.hello.tr
                String[] parts = key.split("\\.");
                if (parts.length >= 3){
                    String pair = parts[0];
                    String word = parts[1];

                // получаем и создаем словарь для этого направления
                Map<String, WordTranslationField> dict = dictionaries.computeIfAbsent(pair, k -> new TreeMap<>());
                // получаем или создаем поле слова
                WordTranslationField field = dict.computeIfAbsent(word, k -> new WordTranslationField());
                // заполняем переводы из .tr

                    String translationStr = properties.getProperty(key, "");
                    field.translations.clear();
                    for(String tr: translationStr.split(",")){
                        field.addTranslation(tr);
                    }

                    String countKey = key.replace(".tr", ".reqCnt");
                    String countStr = properties.getProperty(countKey, "0");
                    try{
                        field.requestsCount = Integer.parseInt(countStr.trim());
                    } catch (NumberFormatException ignored){}
                }
            }
        }
    }
}

