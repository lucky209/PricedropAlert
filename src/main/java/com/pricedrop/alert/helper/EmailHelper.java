package com.pricedrop.alert.helper;

import com.pricedrop.alert.helper.constant.PriceHistoryConstants;
import com.pricedrop.alert.model.PriceHistoryData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class EmailHelper {

    @Value("${to.mail.id}")
    private String[] toMailId;

    @Value("${output.resource.path}")
    private String outputResourcePath;

    @Autowired private JavaMailSender javaMailSender;

    public boolean sendMailWithVideo(List<PriceHistoryData> productList) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(toMailId);
        helper.setSubject(PriceHistoryConstants.MAIL_SUBJECT);
        helper.setText(this.constructMailBody(productList), true);
        for (int i = 1; i <= productList.size(); i++) {
            helper.addAttachment(
                    "video-" + i + ".mp4",
                    new File(outputResourcePath + "/video-"+ i +".mp4"));
        }
        helper.addAttachment("Full-video.mp4", new File(outputResourcePath + "/merged.mp4"));
        javaMailSender.send(mimeMessage);
        log.info("Mail has sent successfully...");
        return true;
    }

    private String constructMailBody(List<PriceHistoryData> productList) {
        StringBuilder body = new StringBuilder();
        int count = 1;
        body.append("Product link(s):").append("<br/><br/>");
        for (PriceHistoryData product : productList) {
            if (product.getProductName() == null)
                product.setProductName(PriceHistoryConstants.MAIL_EMPTY_PRODUCT_NAME);
            body.append(count)
                    .append(".")
                    .append(PriceHistoryConstants.SINGLE_SPACE)
                    .append(product.getProductName())
                    .append("<br/>")
                    .append(product.getSiteUrl())
                    .append("<br/><br/>");
            count++;
        }
        String header = PriceHistoryConstants.MAIL_HEADER
                .replace("{}", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm")));
        return header + body;
    }
}
