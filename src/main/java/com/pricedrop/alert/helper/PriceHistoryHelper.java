package com.pricedrop.alert.helper;

import com.pricedrop.alert.helper.constant.PriceHistoryConstants;
import com.pricedrop.alert.model.PriceHistoryData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Map.Entry.comparingByValue;

@Component
@Slf4j
public class PriceHistoryHelper {

    @Autowired private SchedulerHelper schedulerHelper;

    public List<PriceHistoryData> getLatestProducts(WebDriver browser) {
        List<PriceHistoryData> priceHistoryDataList = new ArrayList<>();
        browser.get(PriceHistoryConstants.DEALS_URL);
        WebElement mainDiv = browser.findElement(By.id(PriceHistoryConstants.MAIN_PRODUCT_DIV_ID));
        //fetching product elements
        List<WebElement> productElements = mainDiv.findElements(By.cssSelector(
                PriceHistoryConstants.SINGLE_PRODUCT_CSS_SELECTOR));
        productElements.forEach(element -> {
            PriceHistoryData priceHistoryData = new PriceHistoryData();
            priceHistoryData.setPhUrl(this.fetchPhUrl(element));
            priceHistoryData.setSiteUrl(this.fetchSiteUrl(element));
            priceHistoryData.setPrice(this.fetchPriceHistoryPrice(element));
            priceHistoryDataList.add(priceHistoryData);
        });
        log.info("Fetched {} latest products", priceHistoryDataList.size());
        return priceHistoryDataList;
    }

    private String fetchPriceHistoryPrice(WebElement element) {
        return element
                .findElement(By.className(PriceHistoryConstants.PRICE_CLASS)).getText().trim();
    }

    private String fetchSiteUrl(WebElement element) {
        WebElement elementProductName = element.findElement(By.cssSelector(
                PriceHistoryConstants.PRODUCT_NAME_CSS_SELECTOR));
        return elementProductName.findElement(By.tagName(PriceHistoryConstants.TAG_ANCHOR))
                .getAttribute(PriceHistoryConstants.ATTRIBUTE_HREF);
    }

    private String fetchPhUrl(WebElement element) {
        return element != null ? element.findElement(By.className(
                        PriceHistoryConstants.PRICE_HISTORY_URL_CLASS))
                .getAttribute(PriceHistoryConstants.ATTRIBUTE_HREF) : null;
    }

    public List<PriceHistoryData> fetchDetailedPriceHistoryDetails(WebDriver browser,
                                                                   List<PriceHistoryData> priceHistoryDataList) {
        priceHistoryDataList.forEach(product -> {
            browser.get(product.getPhUrl());
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {
                //ignore
            }
            product.setLowestPrice(this.fetchLowestPrice(browser));
            product.setHighestPrice(this.fetchHighestPrice(browser));
            product.setDropChances(this.fetchDropChances(browser));
            try {
                this.fetchDropDetails(browser, product);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        log.info("Fetched PriceHistoryDetails successfully...");
        return priceHistoryDataList;
    }

    private void fetchDropDetails(WebDriver browser, PriceHistoryData product)
            throws InterruptedException {
        String currentPrice = null; String currentDate = null;
        String priceDropDate; String priceDropPrice;
        Map<String, Integer> dropDetails = new HashMap<>();
        Actions actions = new Actions(browser);
        boolean isLoaded = this.loadCurrentPriceElement(browser);
        if (!isLoaded) {
            return;
        }
        WebElement cpDotElement = this.getCurrentPriceDottedElement(browser);
        if (cpDotElement != null) {
            Dimension dimension = cpDotElement.getSize();
            int width = dimension.getWidth() / 2;
            this.moveOverElementByOffset(cpDotElement, width, actions);
            //get current price
            List<WebElement> textElements = browser.findElements(By.tagName("text"));
            for (WebElement textElement : textElements) {
                if (textElement.getAttribute("x").equals("8")) {
                    List<WebElement> childElements = textElement.findElements(By.xpath("./*"));
                    if (childElements.size() == 4) {
                        currentDate = childElements.get(0).getAttribute(PriceHistoryConstants.ATTRIBUTE_INNER_HTML).trim();
                        currentPrice = childElements.get(3).getAttribute(PriceHistoryConstants.ATTRIBUTE_INNER_HTML).trim();
                        break;
                    }
                }
            }
            //move inside and find last price changed node
            for (int j=1;j<=7;j++) {
                this.moveOverElementByOffset(cpDotElement, width -(j*8), actions);
                textElements = browser.findElements(By.tagName("text"));
                for (WebElement textElement : textElements) {
                    if (textElement.getAttribute("x").equals("8")) {
                        List<WebElement> childElements = textElement.findElements(By.xpath("./*"));
                        if (childElements.size() == 4) {
                            priceDropDate = childElements.get(0).getAttribute(PriceHistoryConstants.ATTRIBUTE_INNER_HTML).trim();
                            priceDropPrice = childElements.get(3).getAttribute(PriceHistoryConstants.ATTRIBUTE_INNER_HTML).trim();
                            if (currentPrice != null && schedulerHelper.convertStringRupeeToInteger(priceDropPrice)
                                    > schedulerHelper.convertStringRupeeToInteger(currentPrice)) {
                                if (!currentDate.equals(priceDropDate)) {
                                    dropDetails.put(priceDropDate, schedulerHelper.convertStringRupeeToInteger(priceDropPrice));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        //set regular price & pricedrop since
        if (!dropDetails.isEmpty()) {
            Optional<Map.Entry<String, Integer>> optional = dropDetails
                    .entrySet().stream().max(comparingByValue());
            optional.ifPresent(entry -> {
                product.setRegularPrice(entry.getValue().toString());
                product.setPricedropSince(entry.getKey());
            });
        }
    }

    private boolean loadCurrentPriceElement(WebDriver browser) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(browser, 15);
        WebElement cpElement = wait.until(ExpectedConditions.presenceOfElementLocated(
                By.id("currentPrice")));
        int count = 1;
        while (StringUtils.isBlank(cpElement.getText()) ||
                cpElement.getText().trim().equalsIgnoreCase("Checking...")) {
            cpElement = browser.findElement(By.id("currentPrice"));
            count++;
            Thread.sleep(1000);
            if (count > 20) {
                return false;
            }
        }
        return true;
    }

    private WebElement getCurrentPriceDottedElement(WebDriver browser) {
        List<WebElement> highCharts = browser.findElements(By.className("highcharts-plot-line"));
        if (!highCharts.isEmpty()) {
            for (WebElement element : highCharts) {
                if (element.getAttribute("stroke") != null) {
                    if (element.getAttribute("stroke").equalsIgnoreCase("purple")) {
                        return element;
                    }
                }
            }
        }
        return null;
    }

    private synchronized void moveOverElementByOffset(WebElement element, int width, Actions actions) {
        actions.moveToElement(element, width, 0);
        actions.moveToElement(element, width, 0);
        actions.build().perform();
    }

    private String fetchDropChances(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.id(PriceHistoryConstants.DROP_CHANCES_ID));
        if (!elements.isEmpty()) {
            return elements.get(0).getText().trim();
        }
        return null;
    }

    private String fetchHighestPrice(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.id(PriceHistoryConstants.HIGHEST_PRICE_ID));
        if (!elements.isEmpty()) {
            return elements.get(0).getText().trim();
        }
        log.info("Cannot fetch highest price for the url {}", browser.getCurrentUrl());
        return null;
    }

    private String fetchLowestPrice(WebDriver browser) {
        List<WebElement> elements = browser.findElements(By.id(PriceHistoryConstants.LOWEST_PRICE_ID));
        if (!elements.isEmpty()) {
            return elements.get(0).getText().trim();
        }
        log.info("Cannot fetch lowest price for the url {}", browser.getCurrentUrl());
        return null;
    }
}
