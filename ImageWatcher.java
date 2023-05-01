import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

import org.apache.commons.io.monitor.*;
import org.apache.commons.io.monitor.FileAlterationObserver;


public class ImageWatcher {
    public static void main(String[] args) throws Exception{
        String folderPath = "/Users/carlosdelgado/Desktop/Work_Images";
        String recipientEmail = "servicerequest@furnishaz.com";

        File folder = new File(folderPath);

        FileAlterationObserver observer = new FileAlterationObserver(folder);
        observer.addListener(new FileAlterationListenerAdaptor(){
            @Override
            public void onFileCreate(File file){
                try{
                    String attachmentPath = file.getAbsolutePath();
                    String fileName = file.getName();
                    String filePath = file.getParentFile().getAbsolutePath();
                    String subject = "New Image Uploaded";
                    String body = "New Image Uploaded: " + fileName + "\n" + "File Path: " + filePath;
                    sendEmail(recipientEmail, subject, body, attachmentPath);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });

        FileAlterationMonitor monitor = new FileAlterationMonitor(5000);
        monitor.addObserver(observer);
        monitor.start();
    }
    
    private static void sendEmail(String recipientEmail, String subject, String body, String attachmentPath) throws MessagingException, IOException {
        String senderEmail = ""; //replace with your email
        String senderPassword = ""; //replace password with your own password from your email. 

        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com"); //replace with your smtp server for your email account
        properties.put("mail.smtp.port", "587"); //replace with your port number for your email account
        properties.put("mail.smtp.starttls.enable", "true"); //enable TLS encryption

        //create a session
        Session session = Session.getInstance(properties, new Authenticator(){
            @Override
            protected PasswordAuthentication getPasswordAuthentication(){
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        //create the message
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
        message.setSubject(subject);

        //create the message body
        BodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body);

        //create the attachment
        BodyPart attachmentBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(attachmentPath);
        attachmentBodyPart.setDataHandler(new DataHandler(source));
        attachmentBodyPart.setFileName(new File(attachmentPath).getName());

        //add the message body and attachment to the multipart message
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        multipart.addBodyPart(attachmentBodyPart);
        message.setContent(multipart);

        //send the message
        Transport.send(message);
    }
}
