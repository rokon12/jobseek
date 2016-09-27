package com.bazlur.jobseek;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

/**
 * @author Bazlur Rahman Rokon
 * @since 9/27/16.
 */
@Service
public class EmailService {
	public static final String UTF_8 = "UTF-8";

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	@Autowired
	private JavaMailSenderImpl javaMailSender;

	public void sendEmail(String content) {
		log.info("sending email");
		MimeMessage mimeMessage = javaMailSender.createMimeMessage();
		MimeMessageHelper message;
		try {
			message = new MimeMessageHelper(mimeMessage, false, UTF_8);
			message.setTo("anmbrr.bit0112@gmail.com");
			message.setFrom("no-reply@bazlur.com");
			message.setSubject("New Job Alert");
			message.setText(content, true);

			javaMailSender.send(mimeMessage);
		} catch (MessagingException e) {
			log.info("unable to send email", e);
		}
	}
}
