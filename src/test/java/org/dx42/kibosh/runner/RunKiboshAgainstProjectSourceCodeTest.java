package org.dx42.kibosh.runner;

import org.junit.jupiter.api.Test;
import org.dx42.kibosh.rule.Rule;
import org.dx42.kibosh.rule.TextRule;
import org.dx42.kibosh.test.AbstractKiboshTest;

import java.util.List;

class RunKiboshAgainstProjectSourceCodeTest extends AbstractKiboshTest {

    @Test
    void RunKibosh() {
        List<Rule> rules = list(
                TextRule.builder()
                        .name("NoTabs")
                        .description("Use spaces rather than tabs")
                        .illegalString("\t")
                        .build(),
                TextRule.builder()
                        .name("ConsecutiveBlankLines")
                        .description("Do not include consecutive blank/empty lines")
                        .illegalRegularExpression("\\n\\s*\\n\\s*\\n")
                        .build());

        KiboshRunner runner = KiboshRunner.builder().baseDirectory("src").build();
        runner.applyRules(rules);
    }
}
