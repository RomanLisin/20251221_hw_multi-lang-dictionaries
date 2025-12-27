package ru.academy.projects.dictionaries;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.*;

//import ru.academy.projects.dictionaries.WordTranslationField;

public class LangsDictionary {
    private final Map<String, WordTranslationField> dict = new TreeMap<>();
    private final Scanner scanner = new Scanner(System.in);

    static void main(String[] args) {
        new LangsDictionary().run();
    }

    private void run(){
        System.out.println("Словарь");
        System.out.println("Команды: -a слово перевод | слово | -list | -top | -q");


        String input;

        while (!(input = scanner.nextLine().trim()).equals("-q")) {
            if (input.startsWith("-a ")) {
                add(input);
            } else if (input.equals("-list")) {
                list();
            } else if (input.equals("-top")){
                showTop();
            } else if (!input.isEmpty()){
                lookup(input);
            }
        }

        System.out.println("Пока!");
    }

    private void add(String cmd){
        String[] parts = cmd.substring(3).split("\\s+", 2);
        if (parts.length < 2){
            System.out.println("Формат: -a слово перевод1, перевод2");
            return;
        }
        String word = parts[0].toLowerCase();
        WordTranslationField el = dict.computeIfAbsent(word, k -> new WordTranslationField());
        for (String tr : parts[1].split(",")){
            el.addTranslation(tr);
        }
        System.out.println("Добавлено: " + word + " -> " + el);
    }

    private void lookup(String word){
        WordTranslationField el = dict.get(word.toLowerCase());
        if(el != null){
            el.requestsCount++;
            System.out.println(word + " -> " + el);
        } else {
            System.out.println("Слово " + word + " не найдено.");
        }
    }
    private  void list(){
        if(dict.isEmpty()){
            System.out.println("Словарь пуст");
            return;
        }
        dict.forEach((wd, tr) -> System.out.println(wd + " -> " + tr));
    }

    private void showTop(){
        dict.entrySet().stream()
                .filter(el -> el.getValue().requestsCount > 0)
                .sorted(Map.Entry.<String, WordTranslationField>comparingByValue())
                .limit(3)
                .forEach(el -> System.out.println(el.getKey() + " -> " + el.getValue()));
    }
}

