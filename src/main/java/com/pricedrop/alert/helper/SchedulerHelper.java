package com.pricedrop.alert.helper;

import com.pricedrop.alert.helper.constant.PriceHistoryConstants;
import com.pricedrop.alert.helper.enums.ProductCategoryEnum;
import com.pricedrop.alert.model.PriceHistoryData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class SchedulerHelper {

    @Value("${ph.drop.chances.threshold.value}")
    private int dropChancesThresholdVal;

    @Value("${product.clothing.terms}")
    private List<String> productClothing;

    @Value("${product.shoes.slippers.terms}")
    private List<String> productShoes;

    @Value("${product.electronics.terms}")
    private List<String> productElectronics;

    @Value("${product.common.terms}")
    private List<String> productCommonTerms;

    @Value("${product.others.terms}")
    private List<String> productOthers;

    public List<PriceHistoryData> fetchSiteDetails(WebDriver browser,
                                 List<PriceHistoryData> priceHistoryDataList) {
        priceHistoryDataList.forEach(product -> {
            if (product.getSiteUrl().contains(PriceHistoryConstants.FLIPKART_URL) ||
                    product.getSiteUrl().contains(PriceHistoryConstants.AMAZON_URL)) {
                browser.get(product.getSiteUrl());
                this.sleep();
                if (browser.getCurrentUrl().contains(PriceHistoryConstants.FLIPKART_URL)) {
                    product.setProductName(this.fetchFlipkartProductName(browser));
                    product.setSitePrice(this.fetchFlipkartPrice(browser));
                } else {
                    product.setProductName(this.fetchAmazonProductName(browser));
                    product.setSitePrice(this.fetchAmazonPrice(browser));
                }
                product.setAvailable(this.isAvailableToBuy(browser));
                product.setSiteUrl(browser.getCurrentUrl());
            }
        });
        log.info("Fetched SiteDetails successfully...");
        return priceHistoryDataList;
    }

    public void sleep() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
            //ignore
        }
    }

    private String fetchFlipkartProductName(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.className(
                PriceHistoryConstants.FLIPKART_PRODUCT_NAME_CLASS));
        if (!elements.isEmpty()) {
            return elements.get(0).getText().trim();
        }
        log.info("Cannot fetch product name of the flipkart element for the url " + browser.getCurrentUrl());
        return null;
    }

    private String fetchAmazonProductName(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.id(PriceHistoryConstants.AMAZON_PRODUCT_NAME_ID));
        if (!elements.isEmpty()) {
            return elements.get(0).getText().trim();
        }
        log.info("Cannot fetch product name of the amazon element for the url {}", browser.getCurrentUrl());
        return null;
    }

    private String fetchFlipkartPrice(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.cssSelector(
                PriceHistoryConstants.FLIPKART_PRODUCT_PRICE_CSS_CLASS));
        if (!elements.isEmpty()) {
            return elements.get(0).getText().trim();
        }
        return null;
    }

    private String fetchAmazonPrice(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.id(PriceHistoryConstants.AMAZON_PRODUCT_PRICE_ID_1));
        if (!elements.isEmpty()) {
            return elements.get(0).getText().trim();
        } else {
            elements = browser.findElements(By.id(PriceHistoryConstants.AMAZON_PRODUCT_PRICE_ID_2));
            if (!elements.isEmpty()) {
                return elements.get(0).getText().trim();
            } else {
                elements = browser.findElements(By.id(PriceHistoryConstants.AMAZON_PRODUCT_PRICE_ID_3));
                if (!elements.isEmpty()) {
                    return elements.get(0).getText().trim();
                }
            }
        }
        return null;
    }

    void isGoodOffer(List<PriceHistoryData> priceHistoryDataList) {
        priceHistoryDataList.forEach(product -> {
            if (StringUtils.isNotBlank(product.getSitePrice())
                    && StringUtils.isNotBlank(product.getDropChances())
                    && product.isAvailable()) {
                Integer phPrice = this.convertStringRupeeToInteger(product.getPrice());
                Integer sitePrice = this.convertStringRupeeToInteger(product.getSitePrice());
                if (phPrice.equals(sitePrice)) {
                    int dropChances = Integer.parseInt(
                            product.getDropChances().trim().replace(
                                    PriceHistoryConstants.UTIL_PERCENTAGE, PriceHistoryConstants.UTIL_EMPTY_QUOTE));
                    if (dropChances <= dropChancesThresholdVal)
                        product.setGoodOffer(true);
                }
            }
        });
        log.info("Updated Good offer products successfully...");
    }

    public int convertStringRupeeToInteger(String rupee) {
        rupee = rupee
                .replace(PriceHistoryConstants.UTIL_RUPEE, PriceHistoryConstants.UTIL_EMPTY_QUOTE)
                .replaceAll(PriceHistoryConstants.UTIL_COMMA, PriceHistoryConstants.UTIL_EMPTY_QUOTE);
        if (rupee.contains(PriceHistoryConstants.UTIL_DOT)) {
            rupee = rupee.substring(0, rupee.indexOf(PriceHistoryConstants.UTIL_DOT)).trim();
        }
        if (rupee.contains(PriceHistoryConstants.UTIL_HYPHEN)) {
            rupee = rupee.substring(0, rupee.indexOf(PriceHistoryConstants.UTIL_HYPHEN)).trim();
        }
        rupee = rupee.replaceAll(PriceHistoryConstants.SINGLE_SPACE, PriceHistoryConstants.UTIL_EMPTY_QUOTE);
        return Integer.parseInt(rupee.trim());
    }

    private boolean isAvailableToBuy(WebDriver browser) {
        return !(browser.getPageSource().toLowerCase().contains("sold out") ||
                browser.getPageSource().toLowerCase().contains("out of stock") ||
                browser.getPageSource().toLowerCase().contains("currently unavailable"));
    }

    public List<PriceHistoryData> filterTopProducts(List<PriceHistoryData> priceHistoryDataList) {
        //1. it should be available, distinct records, equal to site price, should have regular price
        priceHistoryDataList = priceHistoryDataList
                .stream()
                .distinct()
                .filter(PriceHistoryData::isAvailable)
                .filter(priceHistoryData -> priceHistoryData.getSitePrice() != null && priceHistoryData.getSitePrice().equals(priceHistoryData.getPrice()))
                .filter(priceHistoryData -> priceHistoryData.getRegularPrice() != null)
                .collect(Collectors.toList());
        //2. filter special products in the list
        List<PriceHistoryData> specialProductsList = this.fetchSpecialProductsList(priceHistoryDataList);
        //3.filter by drop chances
        List<PriceHistoryData> dropChancesList = new ArrayList<>();
        if (specialProductsList.size() > 0) {
            for (PriceHistoryData priceHistoryData : specialProductsList) {
                if (priceHistoryData.getDropChances() != null && priceHistoryData.getDropChances().equals("0%")) {
                    dropChancesList.add(priceHistoryData);
                }
            }
        } else {
            for (PriceHistoryData priceHistoryData : priceHistoryDataList) {
                if (priceHistoryData.getDropChances() != null && priceHistoryData.getDropChances().equals("0%")) {
                    dropChancesList.add(priceHistoryData);
                }
            }
        }
        //4.order by price
        if (dropChancesList.size() > 0) {
            dropChancesList = dropChancesList
                    .stream()
                    .sorted(Comparator.comparingInt(a -> a.getProductCategory().priority))
                    .collect(Collectors.toList());
        } else if (specialProductsList.size() > 0) {
            specialProductsList = specialProductsList
                    .stream()
                    .sorted(Comparator.comparingInt(a -> a.getProductCategory().priority))
                    .collect(Collectors.toList());
        } else {
            priceHistoryDataList = priceHistoryDataList
                    .stream()
                    .sorted(Comparator.comparingInt(a -> this.convertStringRupeeToInteger(a.getSitePrice())))
                    .collect(Collectors.toList());
        }
        //5. Return the top product(s)
        if (dropChancesList.size() > 0) {
            log.info("Found {} {} category products.", this.getProductCountByCategory(
                    dropChancesList, ProductCategoryEnum.CLOTHING), ProductCategoryEnum.CLOTHING);
            log.info("Found {} {} category products.", this.getProductCountByCategory(
                    dropChancesList, ProductCategoryEnum.ELECTRONICS), ProductCategoryEnum.ELECTRONICS);
            log.info("Found {} {} category products.", this.getProductCountByCategory(
                    dropChancesList, ProductCategoryEnum.SHOES), ProductCategoryEnum.SHOES);
            log.info("Found {} {} category products.", this.getProductCountByCategory(
                    dropChancesList, ProductCategoryEnum.OTHERS), ProductCategoryEnum.OTHERS);
            log.info("Found {} {} category products.", this.getProductCountByCategory(
                    dropChancesList, ProductCategoryEnum.COMMON), ProductCategoryEnum.COMMON);
            return dropChancesList;
        } else if (specialProductsList.size() > 0) {
            log.info("Found {} {} category products.", this.getProductCountByCategory(
                    specialProductsList, ProductCategoryEnum.CLOTHING), ProductCategoryEnum.CLOTHING);
            log.info("Found {} {} category products.", this.getProductCountByCategory(
                    specialProductsList, ProductCategoryEnum.ELECTRONICS), ProductCategoryEnum.ELECTRONICS);
            log.info("Found {} {} category products.", this.getProductCountByCategory(
                    specialProductsList, ProductCategoryEnum.SHOES), ProductCategoryEnum.SHOES);
            log.info("Found {} {} category products.", this.getProductCountByCategory(
                    specialProductsList, ProductCategoryEnum.OTHERS), ProductCategoryEnum.OTHERS);
            log.info("Found {} {} category products.", this.getProductCountByCategory(
                    specialProductsList, ProductCategoryEnum.COMMON), ProductCategoryEnum.COMMON);
            return specialProductsList;
        } else {
            log.info("No categorized products found....");
            return priceHistoryDataList;
        }
    }

    private long getProductCountByCategory(List<PriceHistoryData> priceHistoryDataList, ProductCategoryEnum productCategoryEnum) {
        return priceHistoryDataList.stream()
                .filter(a -> a.getProductCategory().equals(productCategoryEnum))
                .count();
    }

    private List<PriceHistoryData> fetchSpecialProductsList(List<PriceHistoryData> priceHistoryDataList) {
        List<PriceHistoryData> specialProductsList = new ArrayList<>();
        boolean found;
        //clothing
        for (PriceHistoryData priceHistoryData : priceHistoryDataList) {
            found = false;
            for (String productTerm : productClothing) {
                if (priceHistoryData.getProductName().toLowerCase().contains(productTerm)) {
                    priceHistoryData.setProductCategory(ProductCategoryEnum.CLOTHING);
                    specialProductsList.add(priceHistoryData);
                    found = true;
                    break;
                }
            }
            if (found)
                continue;
            //electronics
            for (String productTerm : productElectronics) {
                if (priceHistoryData.getProductName().toLowerCase().contains(productTerm)) {
                    priceHistoryData.setProductCategory(ProductCategoryEnum.ELECTRONICS);
                    specialProductsList.add(priceHistoryData);
                    found = true;
                    break;
                }
            }
            if (found)
                continue;
            //shoes
            for (String productTerm : productShoes) {
                if (priceHistoryData.getProductName().toLowerCase().contains(productTerm)) {
                    priceHistoryData.setProductCategory(ProductCategoryEnum.SHOES);
                    specialProductsList.add(priceHistoryData);
                    found = true;
                    break;
                }
            }
            if (found)
                continue;
            //common
            for (String productTerm : productCommonTerms) {
                if (priceHistoryData.getProductName().toLowerCase().contains(productTerm)) {
                    priceHistoryData.setProductCategory(ProductCategoryEnum.COMMON);
                    specialProductsList.add(priceHistoryData);
                    found = true;
                    break;
                }
            }
            if (found)
                continue;
            //others
            for (String productTerm : productOthers) {
                if (priceHistoryData.getProductName().toLowerCase().contains(productTerm)) {
                    priceHistoryData.setProductCategory(ProductCategoryEnum.OTHERS);
                    specialProductsList.add(priceHistoryData);
                    break;
                }
            }
        }
        return specialProductsList;
    }

    public PriceHistoryData pickTopProduct(List<PriceHistoryData> priceHistoryDataList) {
        if (priceHistoryDataList.stream().findFirst().isPresent())
            return priceHistoryDataList.stream().findFirst().get();
        return null;
    }

    public String convertToIndianCount(String rupeeWithoutComma) {
        int length = rupeeWithoutComma.length();
        if (length < 4)
            return rupeeWithoutComma;
        else if (length < 6)
            return new StringBuffer(rupeeWithoutComma)
                    .insert(length-3, PriceHistoryConstants.UTIL_COMMA)
                    .toString();
        else if (length < 8)
            return new StringBuffer(rupeeWithoutComma)
                    .insert(length-3, PriceHistoryConstants.UTIL_COMMA)
                    .insert(length-5, PriceHistoryConstants.UTIL_COMMA)
                    .toString();
        else
            return rupeeWithoutComma;
    }
}
