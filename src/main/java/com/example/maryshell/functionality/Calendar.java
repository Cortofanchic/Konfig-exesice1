package com.example.maryshell.functionality;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Класс для вывода календаря в виде строки.
 * <p>
 * Получает и обрабатывет введённые аргументы получая значение
 * текущей даты в формате {@link LocalDate}.
 * <p>
 * При помощи методов возможен вывод календаря на один месяц или один год
 * в формате строки.
 *
 * @author Cortofanchic
 * @version 1.0
 */
public class Calendar {
    private final static int WIDTH = 20;
    private final LocalDate time;
    private boolean allYear = false;
    private boolean allMonth = false;

    /**
     * Конструктор, устанавливающий значение
     * текущей даты в формате {@link LocalDate}.
     *
     * @param arg параметр с необходимой для календаря датой
     * @throws Exception если параметр не корректен и не может быть обработан
     */
    Calendar(List<String> arg) throws Exception {
        time = processArguments(arg);
    }

    /**
     * Регулирует вывод календаря: если введён год без конкретного месяца
     * и дня, то осуществляется вывод всех 12 месяцев, в ином случае
     * возвращается календарь на один месяц.
     *
     * @return строковый календарь на 1 или 12 месяцев
     */
    public String printCal(){
        if (allYear){
            StringBuilder sb = new StringBuilder();
            for (int month=1; month < 13; month++){
                sb.append(printMonth(LocalDate.of(time.getYear(), month, 1)));
                sb.append("\n");
            }
            return sb.toString();
        } else {
            return printMonth(time);
        }
    }

    /**
     * Возвращает строковый календарь на 1 месяц.
     * <p>
     * Выравнивает месяц календаря и год по ширине {@value WIDTH} и
     * возвращает выравненный календарь.
     *
     * @param date дата для печати календаря
     * @return строковый календарь на 1 месяц
     */
    private String printMonth(LocalDate date){
        String monthName = date.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
        String header = monthName + " " + date.getYear();

        StringBuilder sb = new StringBuilder();
        int pad = Math.max(0, (WIDTH - header.length()) / 2);
        sb.append(" ".repeat(pad)).append(header).append("\n");
        sb.append("Пн Вт Ср Чт Пт Сб Вс\n");

        YearMonth ym = YearMonth.of(date.getYear(), date.getMonth());
        LocalDate first = ym.atDay(1);
        int firstDow = first.getDayOfWeek().getValue();
        int days = ym.lengthOfMonth();

        sb.append("   ".repeat(Math.max(0, firstDow - 1)));

        for (int day = 1; day <= days; day++) {
            LocalDate localDate = ym.atDay(day);
            String cell = String.format("%2d ", day);
            if (!allYear && !allMonth && localDate.equals(date)) {
                cell = "[" + String.format("%2d", day) + "]";
            }
            sb.append(cell);
            if ((day + firstDow - 1) % 7 == 0) sb.append("\n");
        }
        if ((days + firstDow - 1) % 7 != 0) sb.append("\n");

        return sb.toString();
    }

    /**
     * Обрабатывает аргументы и, при соответствии требованиям,
     * возвращает результат дополнительной обработки сторонними функциями
     * данных аргументов.
     *
     * @param arguments аргументы для составления даты календаря
     * @return дата в формате {@link LocalDate}
     * @throws Exception если аргументы не соответсвуют требованиям или формату
     */
    private LocalDate processArguments(List<String> arguments) throws Exception {
        return switch (arguments.size()) {
            case 1 -> processOneArgument(arguments.get(0));
            case 2 -> processTwoArgument(arguments.get(0), arguments.get(1));
            case 3 -> processThreeArguments(arguments.get(0), arguments.get(1), arguments.get(2));
            default -> throw new Exception("Error: time argument exception.");
        };
    }

    /**
     * Обрабатывает один аргумент для определения даты.
     *
     * @param argument аргумент для определения введённой даты ("today", "tomorrow", "20XX", "{Month}")
     * @return дату в формате {@link LocalDate}
     * @throws Exception если аргумент не соответсвует требованиям или формату
     */
    private LocalDate processOneArgument(String argument) throws Exception{
        String lowerArgument = argument.toLowerCase(Locale.ROOT);
        if (argument.matches("\\d{4}")) {
            int year = Integer.parseInt(argument);
            allYear = true;
            return LocalDate.ofYearDay(year, 1);
        }

        switch (lowerArgument) {
            case "now", "today": return LocalDate.now();
            case "yesterday": return LocalDate.now().minusDays(1);
            case "tomorrow": return LocalDate.now().plusDays(1);
        }

        for (Month m : Month.values()) {
            String full = m.getDisplayName(TextStyle.FULL, Locale.ENGLISH).toLowerCase();
            String shortName = m.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).toLowerCase();
            if (lowerArgument.equals(full) || lowerArgument.equals(shortName)) {
                allMonth = true;
                return LocalDate.of(LocalDate.now().getYear(), m, 1);
            }
        }

        var pattern = Pattern.compile("([+-]?)(\\d+)\\s*(day|days|week|weeks|month|months|year|years)\\s*(ago|left)?");
        var m = pattern.matcher(lowerArgument);
        if (m.matches()) {
            int sign = "-".equals(m.group(1)) ? -1 : 1;
            if ("ago".equals(m.group(4))) sign = -1;
            int n = Integer.parseInt(m.group(2)) * sign;
            return switch (m.group(3)) {
                case "day", "days"     -> LocalDate.now().plusDays(n);
                case "week", "weeks"   -> LocalDate.now().plusWeeks(n);
                case "month", "months" -> LocalDate.now().plusMonths(n);
                case "year", "years"   -> LocalDate.now().plusYears(n);
                default -> null;
            };
        }
        throw new Exception("Error: time argument exception.");
    }

    /**
     * Обрабатывает два аргумента для определения даты.
     *
     * @param argument1 первый аргумент для определения введённой даты (месяц)
     * @param argument2 второй аргумент для определения введённой даты (год)
     * @return дату в формате {@link LocalDate}
     * @throws Exception если аргументы не соответсвуют требованиям или формату
     */
    private LocalDate processTwoArgument(String argument1, String argument2) throws Exception {
        int month = Integer.parseInt(argument1);
        int year = Integer.parseInt(argument2);
        if (month < 1 || month > 12) {
            throw new Exception("Error: time argument exception.");
        }
        allMonth = true;
        return LocalDate.of(year, month, 1);
    }

    /**
     * Обрабатывает три аргумента для определения даты.
     *
     * @param argument1 первый аргумент для определения введённой даты (день)
     * @param argument2 второй аргумент для определения введённой даты (месяц)
     * @param argument3 третий аргумент для опредедения введённой даты (год)
     * @return дату в формате {@link LocalDate}
     * @throws Exception если аргумент не соответсвует требованиям или формату
     */
    private LocalDate processThreeArguments(String argument1, String argument2, String argument3) throws Exception {
        int day = Integer.parseInt(argument1);
        int month = Integer.parseInt(argument2);
        int year = Integer.parseInt(argument3);
        if ((month < 1 || month > 12) || (day < 1 || day > 31)) {
            throw new Exception("Error: time argument exception.");
        }
        return LocalDate.of(year, month, day);
    }
}
