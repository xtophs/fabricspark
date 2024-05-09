# Microsoft Fabric Scala Samples

This repo hosts sample code for accessing Microsoft Fabric OneLake using the Scala language. The included samples demonstrate:

- Reading a file from OneLake using the ADLS v2 APIs
- Reading a file from OneLake using Spark
- Querying the Lakehouse SQL Endpoint using Spark

## Prerequisites

All prerequisites are installed in the devcontainer.

- Java 8 or higher
- Scala 2.12 or higher
- SBT (Scala Build Tool)
- An IDE that supports Scala (like IntelliJ IDEA or Visual Studio Code with the Scala plugin)

## Setup

1. Set up a Fabric workspace with a Lakehouse. 
1. Add data to the Lakehouse
1. Enable [Service Principal authentication to Microsoft Fabric](https://debruyn.dev/2023/how-to-use-service-principal-authentication-to-access-microsoft-fabrics-onelake/) 
1. Clone the repository to your local machine.
1. Open the project in your IDE.
1. Ensure that the following dependencies are included in your `build.sbt` file:

```scala
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.2.1",
  "org.apache.spark" %% "spark-sql" % "3.2.1",
  "org.apache.hadoop" % "hadoop-azure" % "3.2.1",
  "com.microsoft.azure" % "azure-storage" % "8.6.4",
  "io.delta" %% "delta-core" % "1.2.1",
  "io.delta" %% "delta-storage" % "1.2.1",
  "com.azure.cosmos.spark" %% "azure-cosmos-spark_3-2_2-12" % "4.28.4",
  "com.microsoft.azure.synapse" %% "synapseutils_2.12" % "1.5.1"
)
```

## Running the Program
1. Launch the devcontainer in VS Code by accepting the prompt or by opening the command palette and selecting `Reopen in Container`. For detailed description of the devcontainer see [README-devcontainer.md](README-devcontainer.md)

1. Set the following environment variables in the `.env` file with your actual values:

- `ONELAKE_URL`: This is the URL of your Azure Data Lake Storage. The application will connect to this URL to read data.
- `FILE_SYSTEM_NAME`: This is the name of the file system in your Azure Data Lake Storage that the application will interact with.
- `CONTAINER_NAME`: This is the name of the container in your Azure Data Lake Storage where your data is stored.
- `FILE_NAME`: This is the name of the file that the application will read from the data lake.
- `FILES_DIRECTORY`: This is the directory in your Azure Data Lake Storage where your files are stored.
- `TENANT_ID`: This is the ID of your Azure Active Directory tenant. The application uses this ID to authenticate with Azure.
- `CLIENT_ID`: This is the ID of your Azure Active Directory application. The application uses this ID to authenticate with Azure.
- `CLIENT_SECRET`: This is the secret of your Azure Active Directory application. The application uses this secret to authenticate with Azure.

For a lakehouse with this afbss path `abfss://FileSystemName@onelake.dfs.fabric.microsoft.com/ContainerName/Files`, the variables should look like:
```
ONELAKE_URL=https://onelake.dfs.fabric.microsoft.com
FILE_SYSTEM_NAME=FileSystemName
CONTAINER_NAME=ContainerName
```

1. Run the `Main.scala` file from your IDE, or from the terminal using the following command:

```scala
sbt "runMain Main"
```

This will start the Spark application and read data from your Azure Data Lake Storage.

## Troubleshooting
If you encounter any issues while running the program, check the following:

- Ensure that all the environment variables are set correctly.
- Make sure that your Azure client ID, client secret, and tenant ID are correct and have the necessary permissions to access your Azure Data Lake Storage.
- Check the console output for any error messages or exceptions. These can provide clues about what's going wrong.
If you're still having trouble, feel free to open an issue on this repository and we'll do our best to help you out.