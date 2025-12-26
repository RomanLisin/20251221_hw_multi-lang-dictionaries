package ru.academy.projects.dictionaries;

import java.util.Scanner;

public class LangsDictionary {

   static void main(String[] args) {
        System.out.println("Словарь");
        System.out.println("Введите -q для выхода ");

        Scanner scanner = new Scanner(System.in);
        String input;

        while (!(input = scanner.nextLine().trim()).equals("-q")) {

            System.out.println("Неизвестная команда. Доступно: -q");
        }

        System.out.println("Пока!");
    }
}

