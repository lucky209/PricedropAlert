package com.pricedrop.alert.controller;


import com.pricedrop.alert.helper.EmailHelper;
import com.pricedrop.alert.helper.ImageCreationHelper;
import com.pricedrop.alert.helper.VideoCreationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;
import java.io.IOException;

@RestController
@Slf4j
public class PricedropAlertController {

    @Autowired private ImageCreationHelper imageCreationHelper;
    @Autowired private VideoCreationHelper videoCreationHelper;
    @Autowired private EmailHelper emailHelper;

    @GetMapping("/price-drop-alert")
    public String priceDropAlert() throws Exception {
        // videoCreationHelper.mergeAVCodecMp4(10);
        return "Video created successfullyx";
    }
}
