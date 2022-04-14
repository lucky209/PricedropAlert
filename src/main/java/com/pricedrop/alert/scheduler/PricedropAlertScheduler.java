package com.pricedrop.alert.scheduler;

import com.pricedrop.alert.helper.*;
import com.pricedrop.alert.model.PriceHistoryData;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PricedropAlertScheduler {

    @Autowired private SchedulerHelper schedulerHelper;
    @Autowired private BrowserHelper browserHelper;
    @Autowired private PriceHistoryHelper priceHistoryHelper;
    @Autowired private ImageCreationHelper imageCreationHelper;
    @Autowired private VideoCreationHelper videoCreationHelper;
    @Autowired private EmailHelper emailHelper;

    @Scheduled(cron = "${price.history.scheduler.cron}")
    public void fetchProducts() {
        log.info("Price history scheduler fetching products...");
        WebDriver browser = browserHelper.openBrowser(true);
        try {
            //1. get list of the latest products
            List<PriceHistoryData> priceHistoryDataList = priceHistoryHelper.getLatestProducts(browser);
            if (!priceHistoryDataList.isEmpty()) {

                //2. collect detailed product price history
                priceHistoryDataList = priceHistoryHelper.fetchDetailedPriceHistoryDetails(browser, priceHistoryDataList);

                //3. collect site details
                priceHistoryDataList = schedulerHelper.fetchSiteDetails(browser, priceHistoryDataList);

                //4. Pick top product
                priceHistoryDataList = schedulerHelper.filterTopProducts(priceHistoryDataList);
                if (priceHistoryDataList.size() > 10)
                    priceHistoryDataList = priceHistoryDataList.stream().limit(10).collect(Collectors.toList());
                log.info("Filtered to {} products", priceHistoryDataList.size());
                //5. Create image
                boolean success = imageCreationHelper.createProductImages(priceHistoryDataList, browser);
                browser.quit();
                log.info("Is output image(s) created successfully : " + success);
                //6. Create video
                if (success)
                    success = videoCreationHelper.createMP4WithAudio(priceHistoryDataList.size());
                log.info("Is output video(s) created successfully : " + success);
                if (success)
                   success = videoCreationHelper.mergeAVCodecMp4(priceHistoryDataList.size());
                log.info("Is merged video created successfully : " + success);
                //7. Send mail with video
                if (success)
                    success = emailHelper.sendMailWithVideo(priceHistoryDataList);
                log.info("Is mail send successfully : " + success);

            }
        } catch (Exception ex) {
            log.info("Exception occurred. Exception is " + ex.getMessage());
        }
        browser.quit();
        log.info("Scheduler executed successfully....");
    }
}
