package com.hhu.tool;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailTool {

    private final JavaMailSenderImpl javaMailSender;

    @Tool("邮件发送功能")
    public String emailSend(@P("发送的目标邮箱")String targetEmail,
                          @P("标题")String subject,
                          @P("具体的内容")String context){
        //用字符串模板的方式来输出内容
        String format = String.format("""
                        邮件发送服务被调用!
                        - 收件人邮箱: %s
                        -  发送内容: %s
                        -  发送主题: %s
                        """,
                targetEmail,
                context,
                subject);
        log.info("邮件功能被调用,对应信息:\n{}",format);
        MimeMessage message = javaMailSender.createMimeMessage();


        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setSubject(subject);
            helper.setText(context);
            helper.setTo(targetEmail);
            /// TODO 这里改成要发送的邮箱
            helper.setFrom("Agent" + '<' + "这里改成要发送的邮箱@QQ.com" + '>');
            javaMailSender.send(message);
            log.info("邮件已成功发送到:{}", targetEmail, context);
        } catch (Exception e) {
            log.error("发送邮件失败{}", e);
        }

        return format;
    }
}
