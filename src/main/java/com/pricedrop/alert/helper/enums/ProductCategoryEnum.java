package com.pricedrop.alert.helper.enums;

public enum ProductCategoryEnum {

    CLOTHING(1,"clothing"),
    ELECTRONICS(2,"electronics"),
    SHOES(3,"shoes"),
    COMMON(4,"common"),
    OTHERS(5,"others");

    public int priority;
    public String category;

    ProductCategoryEnum(int priority, String category) {
        this.priority = priority;
        this.category = category;
    }
}
