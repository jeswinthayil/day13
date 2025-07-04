package in.edu.kristujayanti.utils;

import io.vertx.core.Vertx;
import io.vertx.ext.mail.MailClient;
import io.vertx.ext.mail.MailConfig;
import io.vertx.ext.mail.MailMessage;
import io.vertx.ext.mail.StartTLSOptions;
import io.vertx.ext.mail.LoginOption;

public class MailUtil {

    private final MailClient mailClient;

    public MailUtil(Vertx vertx) {
        MailConfig config = new MailConfig()
                .setHostname("smtp.gmail.com")
                .setPort(587)
                .setStarttls(StartTLSOptions.REQUIRED)
                .setUsername("your-email@gmail.com")
                .setPassword("your-app-password")
                .setLogin(LoginOption.REQUIRED);

        this.mailClient = MailClient.create(vertx, config);
    }

    public void sendMail(String to, String subject, String content) {
        MailMessage message = new MailMessage()
                .setFrom("Your Name <your-email@gmail.com>")
                .setTo(to)
                .setSubject(subject)
                .setText(content);

        mailClient.sendMail(message)
                .onSuccess(res -> System.out.println("📧 Mail sent to " + to))
                .onFailure(err -> System.err.println("❌ Failed to send mail to " + to + ": " + err.getMessage()));
    }
}
