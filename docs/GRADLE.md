# GRADLE HINTS

## Memory

To manage a project with many modules could be useful to increase the Heap used by Gradle. The best
way is to set it into `$GRADLE_HOME/gradle.properties` 

```
org.gradle.jvmargs=-Xmx2g
```

By default `$GRALDE_HOME` is `$USER_HOME/.gradle`.

## Credentials

Every SMC user has to create file `gradle-local.properties` to setup credentials to access company
Nexus Sonatype package registry

```
nexus.username=
nexus.password=
```

In a multi-project environment could be preferable to set these properties into `$GRADLE_HOME/gradle.properties`.


