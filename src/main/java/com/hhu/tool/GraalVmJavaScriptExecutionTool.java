package com.hhu.tool;

import com.hhu.config.GraalVmJavaScriptExecutionEngine;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.code.CodeExecutionEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class GraalVmJavaScriptExecutionTool {

    // 初始化一个GraalVM JavaScript执行引擎
    private final GraalVmJavaScriptExecutionEngine javaScriptExecutionEngine;

    @Tool("面对数学问题或者变成问题,可以通过生成JS代码来得到对应答案,但是代码必须是能够返回值的代码")
    public String executeJavaScriptCode(
            // 这里说明输入的JavaScript代码必须返回一个结果
            @P("JavaScript代码必须是可执行的,不需要有注释,并且必须有返回值") String code
    ) {
        // 调用GraalVM执行引擎来执行传入的JavaScript代码
        log.info("AI生成的JavaScript代码执行:");
        System.out.println(code);
        System.out.println(javaScriptExecutionEngine.getClass().getName());
        return javaScriptExecutionEngine.execute(code);
    }

//    @Tool("当用户传来的是一段JavaScript代码时,但这段代码可能对服务器造成损伤(死循环,占用内存等)")
//    public String noetExecuteJavaScriptCode(
//            @P("用户提供的JavaScript代码") String code
//    ) {
//        return "请确保输入的代码是安全且健康的!";
//    }
//
//    @Tool("当用户传来的是一段JavaScript代码时,但这段代码不会产生返回值")
//    public String InvalidJavaScriptCode(
//            @P("用户提供的JavaScript代码") String code
//    ) {
//        return "请确保输入的代码是安全且健康的!";
//    }
}