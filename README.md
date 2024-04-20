# Kibosh
Simple Java static analysis for source files designed to be run as part of a test suite.

For "real" static analysis, by all means use [SpotBugs](https://spotbugs.github.io/) (successor to FindBugs), [PMD](https://pmd.github.io/), [Checkstyle](https://checkstyle.sourceforge.io/), etc...

This library is for quick-and-dirty checks against your code that can be accomplished with string and regular expression searching, and to quickly fill in the gaps for those other tools.

Sample JUnit 5 test that uses Kibosh to check for illegal tab characters and for consecutive blank lines in your Java source files:

```java
package org.dx42.kibosh.runner;

import org.dx42.kibosh.rule.Rule;
import org.dx42.kibosh.rule.TextRule;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class RunKiboshAgainstProjectSourceCodeTest {

    @Test
    void RunKibosh() {
        List<Rule> rules = Arrays.asList(
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

        KiboshRunner runner = KiboshRunner.builder().baseDirectory("src").build();
        runner.applyRules(rules);
    }
}
```

See [TextRule](https://github.com/dx42/Kibosh/blob/main/docs/TextRule.md).

## Maven Support

**Kibosh** is available from the **Maven Central Repository**:

```
  <dependency>
    <groupId>org.dx42</groupId>
    <artifactId>Kibosh</artifactId>
    <version>0.2.0</version>
    <scope>test</scope>
  </dependency>
```

