package com.pricedrop.alert.helper;

import com.pricedrop.alert.helper.constant.ImageConstants;
import com.pricedrop.alert.helper.constant.PriceHistoryConstants;
import com.pricedrop.alert.model.PriceHistoryData;
import com.twelvemonkeys.image.ResampleOp;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class ImageCreationHelper {

    @Value("${image.width}")
    private int imageWidth;

    @Value("${image.height}")
    private int imageHeight;

    @Value("${input.resource.path}")
    private String inputResourcePath;

    @Value("${output.resource.path}")
    private String outputResourcePath;

    @Autowired private BrowserHelper browserHelper;
    @Autowired private SchedulerHelper schedulerHelper;

    private static final String BG_COLOR = "#03989E";
    private static final String TITLE_COLOR = "#000000";
    private static final String VALUE_COLOR = "#FFBD59";

    public boolean createProductImages(List<PriceHistoryData> productList, WebDriver browser) throws IOException, FontFormatException {
        int count = 1;
        Font alfaSlabOne = Font.createFont(Font.TRUETYPE_FONT, new File(inputResourcePath + "/Alfa Slab One Regular 400.ttf"))
                .deriveFont(49.4f);
        for (PriceHistoryData product : productList) {
            //create buffered image object img
            BufferedImage img = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            //graphics
            Graphics2D g2d = img.createGraphics();
            //draw background
            this.drawBackgroundImage(g2d,BG_COLOR,img.getWidth(), img.getHeight(),img);
            //draw frame
            this.drawRectFrame(g2d,TITLE_COLOR);
            //set image inside frame
            this.fillImageInsideFrame(g2d,product,browser);
            //Today price
            this.drawText(g2d, TITLE_COLOR, alfaSlabOne, 49.4f, "Today's price", 175, 625);
            int rupee = schedulerHelper.convertStringRupeeToInteger(product.getSitePrice());
            String rupeeStr = schedulerHelper.convertToIndianCount(Integer.toString(rupee)) + "/-";
            this.drawText(g2d, VALUE_COLOR, alfaSlabOne, 54.9f,rupeeStr,265,685);
            //regular price
            this.drawText(g2d, TITLE_COLOR, alfaSlabOne, 49.4f, "Regular price", 175, 755);
            this.drawText(g2d, VALUE_COLOR, alfaSlabOne, 54.9f,
                    schedulerHelper.convertToIndianCount(product.getRegularPrice()) + "/-", 265, 815);
            //price drop since
            this.drawText(g2d, TITLE_COLOR, alfaSlabOne, 49.4f, "Price drop since", 145, 885);
            this.drawText(g2d, VALUE_COLOR, alfaSlabOne, 54.9f,
                    product.getPricedropSince()
                            .replaceAll(PriceHistoryConstants.SINGLE_SPACE, PriceHistoryConstants.UTIL_HYPHEN), 185, 945);
            //link in description
            this.drawText(g2d, TITLE_COLOR, alfaSlabOne, 49.4f, "Link in description", 100, 1060);
            //price drop since
            this.drawText(g2d, TITLE_COLOR, alfaSlabOne, 49.4f, "Date", 290, 1140);
            this.drawText(g2d, VALUE_COLOR, alfaSlabOne, 54.9f,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy")), 185, 1200);
            //set logo
            this.drawLogo(g2d, "src/main/resources/input/logo.png", 85, 625);
            g2d.dispose();
            //write image
            this.writeImage(count, img, null);
            count++;
        }
        return true;
    }

    public void createThumbnail() throws IOException {
        BufferedImage thumbnail = new BufferedImage(ImageConstants.THUMBNAIL_WIDTH,ImageConstants.THUMBNAIL_HEIGHT,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = thumbnail.createGraphics();
        //draw BG color
        this.drawBackgroundImage(g2d,ImageConstants.THUMBNAIL_BG_COLOR_CODE,
                thumbnail.getWidth(), thumbnail.getHeight(),thumbnail);
        //add amazon flipkart logo
        BufferedImage amazonLogo = ImageIO.read(new File(inputResourcePath + "/amazon-logo.png"));
        ResampleOp resizeOp = new ResampleOp(900, 490);
        amazonLogo = resizeOp.filter(amazonLogo, null);
        TexturePaint texturePaint = new TexturePaint(amazonLogo, new Rectangle(0,0,900,490));
        g2d.setPaint(texturePaint);
        g2d.fillRoundRect(0,0,900,900,35,35);
        g2d.dispose();
        this.writeImage(null,thumbnail,
                ImageConstants.THUMBNAIL_OUTPUT_PATH+ImageConstants.THUMBNAIL_FILE_NAME);
    }

    private BufferedImage takeProductScreenshotAndResize(PriceHistoryData product, WebDriver browser)
            throws IOException {
        browser.get(product.getSiteUrl());
        schedulerHelper.sleep();
        File file = ((TakesScreenshot) browser).getScreenshotAs(OutputType.FILE);
        BufferedImage imageToScale = ImageIO.read(file);
        ResampleOp resizeOp = new ResampleOp(570, 490);
        return resizeOp.filter(imageToScale, null);
    }

    private void drawText(Graphics2D g2d, String colorCode, Font font, float fontSize, String text,
                          int xPosition, int yPosition) {
        g2d.setColor(Color.decode(colorCode));
        g2d.setFont(font);
        font.deriveFont(fontSize);
        g2d.drawString(text, xPosition, yPosition);
    }

    private void drawLogo(Graphics2D g2d, String imagePath, int xPosition, int yPosition) throws IOException {
        BufferedImage logo = ImageIO.read(new File(imagePath));
        AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f);
        g2d.setComposite(alphaChannel);
        g2d.drawImage(logo, xPosition, yPosition, null);
    }

    private void drawBackgroundImage(Graphics2D g2d, String bgColorCode, int width, int height, BufferedImage img) {
        g2d.setColor(Color.decode(bgColorCode));
        g2d.fillRect(0, 0, width, height);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(img, 0, 0, null);
    }

    private void drawRectFrame(Graphics2D g2d, String frameColorCode){
        g2d.setColor(Color.decode(frameColorCode));
        g2d.setStroke(new BasicStroke(9));
        g2d.drawRoundRect(70,50,580,500,50,50);
    }

    private void fillImageInsideFrame(Graphics2D g2d, PriceHistoryData product, WebDriver browser) throws IOException {
        BufferedImage screenshot = this.takeProductScreenshotAndResize(product, browser);
        TexturePaint texturePaint = new TexturePaint(screenshot, new Rectangle(75,55,570,490));
        g2d.setPaint(texturePaint);
        g2d.fillRoundRect(76,56,570,490,35,35);
    }

    private void writeImage(Integer count, BufferedImage bufferedImage, String fullPath) {
        File f;
        try {
            if (count != null) {
                f = new File(outputResourcePath + "/image-" + count + ".png");
                if (f.exists()) {
                    boolean success = f.delete();
                    if (success)
                        log.info("Existing image-" + count + " deleted successfully...");
                }
                ImageIO.write(bufferedImage, "png", f);
                log.info("{} created successfully.", "image-" + count + ".png");
            }
            if (fullPath != null) {
                f = new File(fullPath);
                if (f.exists()) {
                    boolean success = f.delete();
                    if (success)
                        log.info("Existing image at path {} deleted successfully...", fullPath);
                }
                ImageIO.write(bufferedImage, "png", f);
                log.info("Image at this path {} created successfully.", fullPath);
            }
        } catch(IOException e){
            System.out.println("Error while creating image : " + e);
        }
    }
}
