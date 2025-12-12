package com.teamfoundry.backend.auth.service;

import com.teamfoundry.backend.auth.service.exception.EmployeeRegistrationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Serviço utilitário para envio de e-mails transacionais (código de verificação, reset de password).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@teamfoundry.com}")
    private String fromAddress;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    /**
     * Envia o código numérico usado no fluxo de registo.
     */
    public void sendVerificationCode(String destination, String code) {
        if (!mailEnabled) {
            log.warn("[MAIL DISABLED] Código {} para {}", code, destination);
            return;
        }

        SimpleMailMessage message = baseMessage(destination);
        message.setSubject("Código de verificação TeamFoundry");
        message.setText("""
                Olá!

                O seu código de verificação é: %s

                Se não realizou este pedido, ignore este e-mail.
                """.formatted(code));

        try {
            mailSender.send(message);
            log.info("Email de verificação enviado para {}", destination);
        } catch (MailException ex) {
            log.error("Falha ao enviar email de verificação para {}", destination, ex);
            throw new EmployeeRegistrationException(
                    "Não foi possível enviar o e-mail de verificação. Tente novamente mais tarde.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        }
    }

    /**
     * Envia o código numérico usado no fluxo de recuperação de password.
     */
    public void sendPasswordResetCode(String destination, String code) {
        if (!mailEnabled) {
            log.warn("[MAIL DISABLED] Password reset code {} para {}", code, destination);
            return;
        }

        SimpleMailMessage message = baseMessage(destination);
        message.setSubject("Código para redefinir password - TeamFoundry");
        message.setText("""
                Recebemos um pedido para redefinir a sua password.
                Utilize o código abaixo (válido durante 1 hora):

                %s

                Se não realizou este pedido, ignore este e-mail.
                """.formatted(code));

        try {
            mailSender.send(message);
            log.info("Email de código de reset enviado para {}", destination);
        } catch (MailException ex) {
            log.error("Falha ao enviar email de reset para {}", destination, ex);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Não foi possível enviar o e-mail de recuperação. Tente novamente mais tarde."
            );
        }
    }

    private SimpleMailMessage baseMessage(String destination) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(destination);
        return message;
    }
}
