package com.se.server.service;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
	private static Properties mailServerProperties = null;

	public void generateAndSendEmail(String email, String subject, String html) {
		if (mailServerProperties == null) {
			mailServerProperties = System.getProperties();
			mailServerProperties.put("mail.smtp.port", "587");
			mailServerProperties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
			mailServerProperties.put("mail.smtp.auth", "true");
			mailServerProperties.put("mail.smtp.starttls.enable", "true");
		}
		Session getMailSession = Session.getDefaultInstance(mailServerProperties, null);
		MimeMessage generateMailMessage = new MimeMessage(getMailSession);
		try {
			Transport transport = getMailSession.getTransport("smtp");
			generateMailMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			generateMailMessage.setSubject("Project Tracking System: " + subject);
			generateMailMessage.setContent(html, "text/html; charset=UTF-8");
			transport.connect("smtp.gmail.com", 587, "taipeitech105se@gmail.com", "ujmujm454");
			transport.sendMessage(generateMailMessage, generateMailMessage.getAllRecipients());
			transport.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
