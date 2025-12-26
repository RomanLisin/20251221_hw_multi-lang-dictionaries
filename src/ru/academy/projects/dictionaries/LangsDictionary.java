package ru.academy.projects.dictionaries;

import java.lang.reflect.MalformedParameterizedTypeException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class LangsDictionary {
    private final Map<String, String> dict = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);

   static void main(String[] args) {
       new LangsDictionary().run();
    }

    private void run(){
        System.out.println("Словарь");
        System.out.println("Команды: -a слово перевод | слово | -q");


        String input;

        while (!(input = scanner.nextLine().trim()).equals("-q")) {
            if (input.startsWith("-a ")){
                add(input);
            } else if (!input.isEmpty()){
                lookup(input);
            }
        }

        System.out.println("Пока!");
    }

    private void add(String cmd){
       String[] parts = cmd.substring(3).split("\\s+", 2);
        if (parts.length < 2){
            System.out.println("Формат: -a слово перевод");
            return;
        }
       String word = parts[0].toLowerCase();
       String tr = parts[1];
       dict.put(word, tr);
        System.out.println("Добавлено: " + word + " -> " + tr);
    }

    private void lookup(String word){
        String tr = dict.get(word.toLowerCase());
        if(tr != null){
            System.out.println(word + " -> " + tr);
        } else {
            System.out.println("Слово " + word + " не найдено.");
        }
    }
}

