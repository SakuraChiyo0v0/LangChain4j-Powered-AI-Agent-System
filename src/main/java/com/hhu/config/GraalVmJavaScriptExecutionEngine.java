package com.hhu.config;

import dev.langchain4j.code.CodeExecutionEngine;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.ResourceLimits;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import static org.graalvm.polyglot.HostAccess.UNTRUSTED;
import static org.graalvm.polyglot.SandboxPolicy.CONSTRAINED;

@Slf4j
@Component
public class GraalVmJavaScriptExecutionEngine implements CodeExecutionEngine {

    public String execute(String code) {
        // 准备输出捕获流
        OutputStream outputStream = new ByteArrayOutputStream();

        // 构建安全隔离的JS执行环境
        try (Context context = Context.newBuilder("js")
                .sandbox(CONSTRAINED)       // 启用沙箱
                .allowHostAccess(UNTRUSTED)  // 限制主机访问
                .out(outputStream)           // 重定向输出
                .err(outputStream)           // 重定向错误
                .resourceLimits(
                        ResourceLimits.newBuilder()
                                .statementLimit(3000, source -> true)
                                .build()
                )
                .build()) {

            // 执行代码并获取结果
            Object result = context.eval("js", code).as(Object.class);
            log.info("代码执行结果:{}",result.toString());
            return String.valueOf(result);
        }catch (PolyglotException e){
            // 捕获GraalVM执行错误
            String errorOutput = outputStream.toString();
            log.error("代码执行出错: {}, 输出: {}", e.getMessage(), errorOutput);

            // 组合错误信息返回
            return String.format("""
                生成的代码执行错误:
                - 错误类型: %s
                - 错误信息: %s
                - 程序输出: %s
                """,
                e.getClass().getSimpleName(),
                e.getMessage(),
                errorOutput.trim());
        } catch (Exception e) {
            // 4. 捕获其他意外错误
            log.error("系统异常: {}", e.getMessage());
            return "系统处理代码时发生意外错误: " + e.getMessage();
        }
    }
}