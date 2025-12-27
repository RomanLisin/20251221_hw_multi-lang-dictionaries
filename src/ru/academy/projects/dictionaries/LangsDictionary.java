package ru.academy.projects.dictionaries;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.*;

//import ru.academy.projects.dictionaries.WordTranslationField;

public class LangsDictionary {
    // private final Map<String, WordTranslationField> dict = new TreeMap<>();
    private final Map<String, Map<String, WordTranslationField>> dictionaries = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);
    private String currentLangPair = "en-ru";

    static void main(String[] args) {
        new LangsDictionary().run();
    }

    private Map<String, WordTranslationField> getCurrentDict(){
        return dictionaries.computeIfAbsent(currentLangPair, k -> new TreeMap<>());
    }

    private void run(){
        System.out.println("Словарь");
        System.out.println("Команды: -a слово перевод | слово | -list | -top | -q");


        String input;

        while (!(input = scanner.nextLine().trim()).equals("-q")) {


            if (input.startsWith("-lang ")) {
                String[] parts = input.split("\\s+");
                if (parts.length >=3){
                    setLanguage(parts[1], parts[2]);
                } else {
                    System.out.println("Формат: -lang <from> <to>");
                }
            } else if (input.equals("-a")) {
                add(input);
            } else if (input.equals("-list")) {
                list();
            } else if (input.equals("-top")){
                showTop();
            } else if (input.equals("-pairs")){
                System.out.println("Направления: " + dictionaries.keySet());
            } else if (!input.isEmpty()){
                lookup(input);
            }
        }

        System.out.println("Пока!");
    }

    private void add(String cmd){
        String[] parts = cmd.substring(3).split("\\s+", 2);
        if (parts.length < 2){
            System.out.println("Формат: -a слово перевод1, перевод2, ...");
            return;
        }
        String word = parts[0].toLowerCase();
        String[] translations = parts[1].split(",");

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

}

