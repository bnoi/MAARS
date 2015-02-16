## How to add a non Maven depency to this project

Add .jar file to local Maven repo called `lib/`.
mvn install:install-file -DlocalRepositoryPath=lib/ -DcreateChecksum=true -Dpackaging=jar -Dfile=~/local/mm/ImageJ/plugins/Micro-Manager/MMCoreJ.jar -DgroupId=org.mm -DartifactId=mmcorej -Dversion=1.0

In the pom.xml, you can use:

```
...
    <repositories>
        <repository>
            <id>lib</id>
            <url>file://${basedir}/lib</url>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>org.mm</groupId>
            <artifactId>mmcorej</artifactId>
            <version>1.0</version>
        </dependency>
    </dependencies>
...
```
