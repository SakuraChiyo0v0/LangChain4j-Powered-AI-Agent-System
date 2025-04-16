package GraalVmJavaScriptExecutionTool;

import com.hhu.config.GraalVmJavaScriptExecutionEngine;
import dev.langchain4j.code.CodeExecutionEngine;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class GraalVmJavaScriptExecutionTool {


    CodeExecutionEngine engine = new GraalVmJavaScriptExecutionEngine();
    @Test
    void should_execute_code() {

        String code = """
                function fibonacci(n) {
                    if (n <= 1) return n;
                    return fibonacci(n - 1) + fibonacci(n - 2);
                }
                                
                fibonacci(10)
                """;

        String result = engine.execute(code);

        System.out.println(result);
    }
}