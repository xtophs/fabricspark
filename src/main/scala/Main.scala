import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import com.azure.identity.ClientSecretCredentialBuilder
import java.util.concurrent.Executors
import com.azure.storage.file.datalake.{
  DataLakeDirectoryClient,
  DataLakeFileClient,
  DataLakeFileSystemClient,
  DataLakeServiceClientBuilder,
  DataLakeFileSystemClientBuilder
}
import java.io.ByteArrayOutputStream

object FabricTest {
  def main(args: Array[String]) {
    def readEnvFile(envFile: String): Map[String, String] = {
      scala.io.Source
        .fromFile(envFile)
        .getLines()
        .filter(_.nonEmpty)
        .map { line =>
          println(line)
          val Array(key, value) = line.split("=", 2)
          key -> value
        }
        .toMap
    }

    def createSparkSession(
        clientId: String,
        clientSecret: String,
        tenantId: String
    ): SparkSession = {
      SparkSession.builder
        .appName("Fabric Read")
        .master("local")
        .config(
          "spark.jars.packages",
          "org.apache.hadoop:hadoop-azure:3.2.1," +
            "com.microsoft.azure:azure-storage:8.6.4," +
            "io.delta:delta-core_2.12:1.2.1," +
            "io.delta:delta-storage:1.2.1," +
            "com.azure.cosmos.spark:azure-cosmos-spark_3-2_2-12:4.28.4," +
            "com.microsoft.azure.synapse:synapseutils_2.12:1.5.1"
        )
        .config(
          "spark.sql.extensions",
          "io.delta.sql.DeltaSparkSessionExtension"
        )
        .config(
          "spark.sql.catalog.spark_catalog",
          "org.apache.spark.sql.delta.catalog.DeltaCatalog"
        )
        .config(
          "fs.azure.account.oauth2.client.endpoint",
          s"https://login.microsoftonline.com/$tenantId/oauth2/token"
        )
        .config("fs.azure.account.auth.type", "OAuth")
        .config(
          "fs.azure.account.oauth.provider.type",
          "org.apache.hadoop.fs.azurebfs.oauth2.ClientCredsTokenProvider"
        )
        .config(
          "fs.azure.account.oauth2.client.id",
          clientId
        )
        .config(
          "fs.azure.account.oauth2.client.secret",
          clientSecret
        )
        .getOrCreate()
    }

    def readLakehouseWithSpark(
        clientId: String,
        clientSecret: String,
        tenantId: String,
        tablePath: String
    ): Unit = {
      try {
        val spark =
          createSparkSession(clientId, clientSecret, tenantId)

        val df = spark.read
          .format("delta")
          .load(tablePath)
        df.show(10)

        // Register the DataFrame as a temporary view
        df.createOrReplaceTempView("myAssets")
        println("--- Now with SQL ---")
        val df2 = spark.sql("SELECT * From myAssets")
        df2.show(10)

        spark.stop()
      } catch {
        case ex: Exception =>
          println(s"An error occurred reading the lakehouse with Spark: ${ex.getMessage}")
      }
    }

    def readLakehouseFile(
        clientId: String,
        clientSecret: String,
        tenantId: String,
        oneLakeUrl: String,
        fileSystemName: String,
        containerName: String,
        filesDirectory: String,
        fileName: String
    ): Unit = {
      
      val credential = new ClientSecretCredentialBuilder()
        .clientId(clientId)
        .clientSecret(clientSecret)
        .tenantId(tenantId)
        .build()

      try {
        // Get a service client object
        val serviceClient = new DataLakeServiceClientBuilder()
          .endpoint(oneLakeUrl)
          .credential(credential)
          .buildClient()

        // Show available file systems
        val fileSystems = serviceClient.listFileSystems()
        println("--- Available file systems: ---")
        fileSystems.forEach(fileSystem => println(fileSystem.getName()))

        val filesUrl = s"$oneLakeUrl/$fileSystemName/$containerName/Files"
        // Get a DataLakeDirectoryClient for the specified directory
        val fileSystemClient = new DataLakeFileSystemClientBuilder()
          .endpoint(filesUrl)
          .credential(credential)
          .buildClient()

        val directoryClient =
          fileSystemClient.getDirectoryClient(
            s"/$containerName/Files/$filesDirectory"
          )

        // Get a DataLakeFileClient for the specified file
        val fileClient = directoryClient.getFileClient(fileName)
        val inMemoryStream = new ByteArrayOutputStream()

        // Read the contents of the file
        println(
          s"Reading file: /$containerName/Files/$filesDirectory/$fileName"
        )
        val fileContents = fileClient.read(inMemoryStream)

        println(s"Successfully read file: ${inMemoryStream.size()} bytes.")
      } catch {
        case ex: Exception =>
          println(s"An error occurred reading the lakehouse: ${ex.getMessage}")
      }
    }

    val env = readEnvFile(".env")

    val clientId = env.getOrElse(
      "CLIENT_ID",
      sys.error("Environment variable CLIENT_ID not set")
    )
    val clientSecret = env.getOrElse(
      "CLIENT_SECRET",
      sys.error("Environment variable CLIENT_SECRET not set")
    )
    val tenantId = env.getOrElse(
      "TENANT_ID",
      sys.error("Environment variable TENANT_ID not set")
    )
    val containerName = env.getOrElse(
      "CONTAINER_NAME",
      sys.error("Environment variable CONTAINER_NAME not set")
    )
    val fileSystemName = env.getOrElse(
      "FILE_SYSTEM_NAME",
      sys.error("Environment variable FILE_SYSTEM_NAME not set")
    )
    val oneLakeUrl = env.getOrElse(
      "ONELAKE_URL",
      sys.error("Environment variable ONELAKE_URL not set")
    )
    val filesDirectory = env.getOrElse(
      "FILES_DIRECTORY",
      sys.error("Environment variable FILES_DIRECTORY not set")
    )
    val fileName = env.getOrElse(
      "FILE_NAME",
      sys.error("Environment variable FILE_NAME not set")
    )

    val lakePath =
      s"abfss://$fileSystemName@onelake.dfs.fabric.microsoft.com/$containerName"
    val tablePath = s"$lakePath/Tables/Assets"

    println("Testing Lakehouse access via ADLSv2 API")
    readLakehouseFile(
      clientId,
      clientSecret,
      tenantId,
      oneLakeUrl,
      fileSystemName,
      containerName,
      filesDirectory,
      fileName
    )

    println("Testing Lakehouse access via Spark")
    readLakehouseWithSpark(clientId, clientSecret, tenantId, tablePath)

    println("Done!")
  }
}
