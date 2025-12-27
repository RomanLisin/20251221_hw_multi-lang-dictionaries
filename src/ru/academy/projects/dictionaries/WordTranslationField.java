package ru.academy.projects.dictionaries;
import  java.util.*;
import java.util.ArrayList;

public class WordTranslationField implements Comparable<WordTranslationField> {
    final List<String> translations = new ArrayList<>();
    int requestsCount = 0;

    public void addTranslation(String tr){
        String clean = tr.trim().toLowerCase();

        if(!clean.isEmpty() && !translations.contains(clean)) {
            translations.add(clean);
        }

    }

    @Override
    public String toString() {
        return "[" +
                String.join(", ", translations) +
                "] (использований: " +
                requestsCount + ")";
    }

    @Override
    // чтобы работала эта запись: .sorted(Map.Entry.<String, WordTranslationField>comparingByValue())
    public int compareTo(WordTranslationField other) {
        //сортируем по убыванию requestsCount: самые популярные - выше
        // Integer.compare(other, this) -> для обратноо порядка
        return Integer.compare(other.requestsCount, this.requestsCount);
    }
}
