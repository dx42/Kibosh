package org.dx42.kibosh.runner;

import org.dx42.kibosh.rule.TextRule;
import org.junit.jupiter.api.Test;

class RunKiboshAgainstProjectSourceCodeTest {

    @Test
    void RunKibosh() {
        KiboshRunner runner = KiboshRunner.builder()
                .baseDirectory("src/main/java")
                .baseDirectory("src/test/java")
                .build();
        runner.applyRules(
                TextRule.builder()
                        .name("NoTabs")
                        .description("Use spaces rather than tabs")
                        .illegalString("\t")
                        .build(),
                TextRule.builder()
                        .name("NoConsecutiveBlankLines")
                        .description("Do not include consecutive blank/empty lines")
                        .illegalRegularExpression("\\n\\s*\\n\\s*\\n")
                        .build());
    }
}
