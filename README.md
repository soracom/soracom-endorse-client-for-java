This library provides SIM authentication (AKA) feature with SORACOM Endorse.

## To get pre-build binary
Visit release page (https://github.com/soracom/soracom-endorse-client-for-java/releases) and download archives.

- distributions/soracom-endorse-cli-xxx.zip( and tar)
These archives are application archive that contains "soracom-endorse" command.

- libs/soracom-endorse.jar
Fat jar type of application archive. You can run endorse cli with "java -jar soracom-endorse.jar" command.

You can run the binaries with Java7 runtime or later.

## How to build SORACOM Endorse client for Java
User can use Gradle to build the project. If you want to build the project from source code, execute following command after checkout.
 
```sh
./gradlew build
```

You can generate configuration file for IDEs with following command.

```sh
// for Eclipse
./gradlew eclipse

// for IntelliJ
./gradrew idea
```

After running this command, you can import this project to your IDE.

Also you can generate application zip archive with following command.

```sh
./gradlew dist
```

after that, you can see following archives in build directory.

- distributions/soracom-endorse-cli-xxx.zip( and tar)
These archives are application archive that contains "soracom-endorse" command.

- libs/soracom-endorse.jar
Fat jar type of application archive. You can run endorse cli with "java -jar soracom-endorse.jar" command.

- libs/soracom-endorse-client-for-java-xxxx.jar
Endorse client library to provide endorse api to other application. Also you can use this library with following gradle setting.

```java
apply plugin: 'java'

repositories {
  jcenter()
  maven { url 'https://soracom.github.io/maven-repository/' }
}

dependencies {
    compile "io.soracom:soracom-endorse-client-for-java-core:0.1.0"
}
```

The soracom-endorse-client-for-java is released under version 2.0 of Apache License
