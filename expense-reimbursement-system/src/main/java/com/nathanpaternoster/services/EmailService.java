package com.nathanpaternoster.services;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Service class to send emails from "notifcationsers@gmail.com"
 */
public class EmailService {
    private static final String username = "";  // removed
    private static final String password = "";  // removed

    private Session getSession() {
        Properties pros = new Properties();
        pros.put("mail.smtp.host", "smtp.gmail.com");
        pros.put("mail.smtp.port", 587);
        pros.put("mail.smtp.auth", "true");
        pros.put("mail.smtp.starttls.enable", "true");
        return Session.getInstance(pros,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
    }

    public void sendEmail(String to, String subject, String body) throws RuntimeException {
        Session session = getSession();
        Message message = new MimeMessage(session);
        try {
            message.setFrom(new InternetAddress("notificationsERS@gmail.com"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);
            message.setText(body);
            Transport.send(message);
        } catch (AddressException e) {
            throw new RuntimeException("Invalid email address entered");
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email");
        }
    }
}
