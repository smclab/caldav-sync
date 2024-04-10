# GRADLE HINTS

## Memoria

Per gestire progetti con un numero elevato di moduli puo' essere utile
aumentare la memoria utilizzata da Gradle.

Se il processo va in errore modificare il file `.settings/org.eclipse.buildship.core.prefs`, 
nella root del clone, per valorizzare la direttiva come da esempio

```
jvm.arguments=-Xmx2g
```

Alternativamente &egrave; possibile agire nel file `$GRADLE_HOME/gradle.properties` aggiungendo
la seguente riga

```
org.gradle.jvmargs=-Xmx2g
```

In questo caso pu&ograve; capitare che gradle sia costretto a forkare continuamente il processo
per gestire tutte le casistiche di utilizzo. In pratica &egrave; preferibile agire su 
`org.eclipse.buildship.core.prefs`.

La variabile `$GRADLE_HOME` corrisponde con la cartella `$USER_HOME/.gradle`


## Credenziali

Ogni utente deve crearsi il file `gradle-local.properties` nel quale indicare le proprie
credenziali di accesso a Nexus Sonatype sfruttando le seguenti property

```
nexus.username=
nexus.password=
```

Se l'utente deve gestire pi&ugrave; progetti pu&ograve; indicare queste property nel
file `$GRADLE_HOME/gradle.properties`.


