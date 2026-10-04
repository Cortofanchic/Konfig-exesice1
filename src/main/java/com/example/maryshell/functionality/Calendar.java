package com.example.maryshell.functionality;

import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


public class Calendar {
    private final static int width = 20;
    private final LocalDate time;
    private boolean allYear = false;
    private boolean allMonth = false;

    Calendar(List<String> arg) throws Exception {
        time = processArguments(arg);
    }

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

    private String printMonth(LocalDate date){
        String monthName = date.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault());
        String header = monthName + " " + date.getYear();

        StringBuilder sb = new StringBuilder();
        int pad = Math.max(0, (width - header.length()) / 2);
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

    private LocalDate processArguments(List<String> arguments) throws Exception {
        return switch (arguments.size()) {
            case 1 -> processOneArgument(arguments.get(0));
            case 2 -> processTwoArgument(arguments.get(0), arguments.get(1));
            case 3 -> processThreeArguments(arguments.get(0), arguments.get(1), arguments.get(2));
            default -> throw new Exception("Error: time argument exception.");
        };
    }

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

    private LocalDate processTwoArgument(String argument1, String argument2) throws Exception {
        int month = Integer.parseInt(argument1);
        int year = Integer.parseInt(argument2);
        if (month < 1 || month > 12) {
            throw new Exception("Error: time argument exception.");
        }
        allMonth = true;
        return LocalDate.of(year, month, 1);
    }

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
