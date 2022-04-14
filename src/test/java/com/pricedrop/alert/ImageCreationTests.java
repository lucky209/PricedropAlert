package com.pricedrop.alert;

import com.pricedrop.alert.helper.ImageCreationHelper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;

@SpringBootTest
public class ImageCreationTests {

    @Autowired
    private ImageCreationHelper helper;

    @Test
    public void thumbnailCreationTest() throws IOException {
        helper.createThumbnail();
    }
}
