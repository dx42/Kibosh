package org.kibosh.runner;

import org.junit.jupiter.api.Test;
import org.kibosh.rule.Rule;
import org.kibosh.rule.TextRule;
import org.kibosh.test.AbstractKiboshTest;

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
                        .description("Do not include more than one blank/empty lines")
                        .illegalRegularExpression("\\n\\s*\\n\\s*\\n")
                        .build());

        KiboshRunner runner = new KiboshRunner("src");
        runner.applyRules(rules);
    }
}
