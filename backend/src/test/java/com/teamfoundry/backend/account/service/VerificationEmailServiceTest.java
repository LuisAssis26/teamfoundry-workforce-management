package com.teamfoundry.backend.account.service;

import com.teamfoundry.backend.auth.service.VerificationEmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Testa o serviço que envia e-mails de verificação.
 */
@ExtendWith(MockitoExtension.class)
class VerificationEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private VerificationEmailService verificationEmailService;

    @BeforeEach
    void setUp() {
        // enable mail sending path in the service (fields are not injected by Spring in this unit test)
        ReflectionTestUtils.setField(verificationEmailService, "mailEnabled", true);
        ReflectionTestUtils.setField(verificationEmailService, "fromAddress", "no-reply@test.local");
    }

    @Test
    @DisplayName("sendVerificationCode envia email com destinatário e código corretos")
    void sendVerificationCode() {
        verificationEmailService.sendVerificationCode("user@example.com", "123456");

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertThat(message.getTo()).containsExactly("user@example.com");
        assertThat(message.getText()).contains("123456");
    }
}
