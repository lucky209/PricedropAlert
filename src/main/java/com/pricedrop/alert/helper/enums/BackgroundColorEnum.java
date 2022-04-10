package com.pricedrop.alert.helper.enums;

import java.time.LocalDate;

public enum BackgroundColorEnum {
    SUNDAY("#03989E"),
    MONDAY("#00C2CB"),
    TUESDAY("#38B6FF"),
    WEDNESDAY("#FF5757"),
    THURSDAY("#FF66C4"),
    FRIDAY("#A6A6A6"),
    SATURDAY("#2471D8"),
    DEFAULT("#FF914D"),
    ;

    private String palette;

    BackgroundColorEnum(String s) {
    }

    public static String getTodayColor(){
       switch (LocalDate.now().getDayOfWeek()) {
           case SUNDAY:
               return SUNDAY.palette;
           case MONDAY:
               return MONDAY.palette;
           case TUESDAY:
               return TUESDAY.palette;
           case WEDNESDAY:
               return WEDNESDAY.palette;
           case THURSDAY:
               return THURSDAY.palette;
           case FRIDAY:
               return FRIDAY.palette;
           case SATURDAY:
               return SATURDAY.palette;
       }
       return DEFAULT.palette;
    }
}
