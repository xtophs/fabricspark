# Scala/Spark Dev Container for VSCode

[Scala Metals](https://scalameta.org/metals/docs/editors/vscode) - IDE Integration for VSCode
[Installing Scala](https://docs.scala-lang.org/getting-started/index.html#using-vscode-with-metals)
[Coursier](https://get-coursier.io/docs/cli-overview) - Artifact manager for scala
[Learing Spark V2 Examples](https://github.com/databricks/LearningSparkV2) - Code examples from Oreily book.

To make all this work, the appropriate version of java, spark, and scala have to be installed. Spark has multiple builds that target specific versions of scala. Spark is also only compatible with Java 8, 11, and 17. If you dont install the exact build for the version of scala used, this all fails. The container uses

Java 17
Scala 2.13
[Spark 3.3.2 for Scala 2.13](https://dlcdn.apache.org/spark/spark-3.3.2/spark-3.3.2-bin-hadoop3-scala2.13.tgz)

Note there is a spark 3.3.2 with no scala qualifier which targets scala 2.12

Spark for Scala 2.13 is important to target because Scala 3 is backwards compatible with libraries built for Scala 2.13 but, not for Scala 2.12. See [this article](https://xebia.com/blog/using-scala-3-with-spark/) for more info.

**Scala 3 syntax is VERY different from Scala 2**. Its syntax however looks closer to python as indentation seems to indicate closures instead of curly braces. The syntax in 3 is even more concise.

## Setup

1. Open the directory as a dev container
1. Once dev container is open, the metals language server should start to download. If it doesnt, click the metals extension tab (letter M icon).
1. VS Code should detect the project prompt to "Import Build". Select import build to start the build server and make metals aware of the project.

## Build

Metals uses a build server called "bloop". Bloop will build automatically as you save files and report build errors. There is no need to manually trigger a build.

There is also a tool named 'sbt' that is equivilent to Maven (actually seems to wrap Maven and Ivy). Sbt will be responsible for installing dependencies and packaging the project into jar.

## Pack

Run `sbt clean package` at bash terminal.

## Debug

Set breakpoints and press F5. The launch configuration is set to load up the MnMCount class. The debugger will instantiate a spark job and attach debugger so you can debug while spark job is executing

## Run (no debug)

It is possible to run the source as a spark job locally with no debugger attached. The job in this context will execute much faster than it does with debugger attached.

Open a bash terminal 
1. Run `PATH=$PATH:/opt/spark/bin`. See known issues
1. Run `sbt clean package`. This compiles source to jar
2. Run `cp target/scala-2.13/foospark_2.13-1.0.jar jars/`. This copies the jar to easier to access directory
3. Run `spark-submit --class MnMCount jars/foospark_2.13-1.0.jar data/mnm_dataset.csv`. This submits the job and provides the csv dataset as arg to the job.

## Known Issue

The spark bin is not attached to the PATH variable for the container environment. This makes bash unable to find `spark-submit` `spark-shell` etc. Ideally this should be set automatically for container environment so spark binaries are easy to execute from bash terminal.

While Spark claims to be compatible with Java 17, there are still some dependencies to earlier versions of java that cannot be loaded in java 17. See this [stackoverflow for the issue](https://stackoverflow.com/questions/73465937/apache-spark-3-3-0-breaks-on-java-17-with-cannot-access-class-sun-nio-ch-direct). **This only seems to occur when debugger is attached and not when you submit jar to spark.**  The workaround is to pass options to the jvm executing the job (or downgrade to Java 8 or 11). This has been configured in `launch.json` to happen automatically when starting debug.

```json
"jvmOptions": [
    "--add-opens=java.base/java.lang=ALL-UNNAMED",
    "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
    "--add-opens=java.base/java.lang.reflect=ALL-UNNAMED",
    "--add-opens=java.base/java.io=ALL-UNNAMED",
    "--add-opens=java.base/java.net=ALL-UNNAMED",
    "--add-opens=java.base/java.nio=ALL-UNNAMED",
    "--add-opens=java.base/java.util=ALL-UNNAMED",
    "--add-opens=java.base/java.util.concurrent=ALL-UNNAMED",
    "--add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED",
    "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
    "--add-opens=java.base/sun.nio.cs=ALL-UNNAMED",
    "--add-opens=java.base/sun.security.action=ALL-UNNAMED",
    "--add-opens=java.base/sun.util.calendar=ALL-UNNAMED",
    "--add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED"
]
```