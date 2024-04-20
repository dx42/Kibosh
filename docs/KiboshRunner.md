# KiboshRunner

The `KiboshRunner` class is the way to execute Kibosh with a set of rules.

It includes the following fields:

| **Field (property)**     | **Description and Usage**                                                                                                                       |
|--------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------|
| baseDirectory            | The base directory for searching for source files. You can specify more that one of these.                                                      |

The `KiboshRunner` class provides a *Builder* API and an `applyRules(List<Rule> rules)` method to execute using a list of Kibosh Rules. Here is an example instantiation and invocation of `applyRules`:

```java
        List<Rule> rules = ...
                
        KiboshRunner runner = KiboshRunner.builder()
                .baseDirectory("src/main/java")
                .baseDirectory("src/test/java")
                .build();
        runner.applyRules(rules);

```
