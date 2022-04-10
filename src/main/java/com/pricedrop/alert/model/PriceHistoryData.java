package com.pricedrop.alert.model;

import com.pricedrop.alert.helper.enums.ProductCategoryEnum;
import lombok.Data;

@Data
public class PriceHistoryData {
    private String phUrl;
    private String siteUrl;
    private String price;
    private String highestPrice;
    private String lowestPrice;
    private String dropChances;
    private String productName;
    private String sitePrice;
    private boolean isAvailable;
    private boolean isGoodOffer;
    private ProductCategoryEnum productCategory;
    private String regularPrice;
    private String pricedropSince;
}
