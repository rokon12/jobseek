package com.bazlur.jobseek;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/27/16.
 */
@Service
public class EmailService {

    @Autowired
    private JavaMailSenderImpl javaMailSender;

    public void sendEmail() {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

    }
}
