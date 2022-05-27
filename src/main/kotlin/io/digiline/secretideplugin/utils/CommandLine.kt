import com.intellij.execution.RunManager
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.project.Project
import com.intellij.util.io.exists
import com.intellij.util.io.readText
import com.intellij.util.io.write
import io.digiline.secretideplugin.SecretNetworkContractConfigurationFactory
import io.digiline.secretideplugin.SecretNetworkContractConfigurationType
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant

/**
 * Clones a repository into the specified path.
 *
 * @param repo the repository to clone
 * @param path the path to clone the repository to
 * @param projectName the name of the project
 */
fun cloneRepo(repo: String, path: Path, projectName: String, doReplace: Boolean = true) {
  val tmpPath = getTmpDir()
  Files.createDirectory(tmpPath)
  doClone(repo, tmpPath)
  if (doReplace) updateCargoToml(tmpPath, projectName)
  removeGitFolder(tmpPath)
  gitInit()
  quickDelete(path.resolve("Cargo.lock").toString())
  moveAllFilesFromFolderToFolder(tmpPath, path)
  quickDelete(tmpPath.toString())
}

private fun gitInit(): Int {
  val commandLine = GeneralCommandLine()
  commandLine.exePath = "/usr/bin/git"
  commandLine.addParameter("init")
  return commandLine.createProcess().waitFor()
}

private fun doClone(repo: String, tmpPath: Path): Int {
  val commandLine = GeneralCommandLine()
  commandLine.exePath = "/usr/bin/git"
  commandLine.addParameter("clone")
  commandLine.addParameter(repo)
  commandLine.addParameter(tmpPath.toString())
  return commandLine.createProcess().waitFor()
}

private fun quickDelete(file: String): Int {
  val commandLine = GeneralCommandLine()
  commandLine.exePath = "rm"
  commandLine.addParameter("-rf")
  commandLine.addParameter(file)
  return commandLine.createProcess().waitFor()
}

private fun removeGitFolder(path: Path): Int {
  val commandLine = GeneralCommandLine()
  commandLine.exePath = "rm"
  commandLine.addParameter("-rf")
  commandLine.addParameter(path.resolve(".git").toString())
  commandLine.createProcess().waitFor()
  return commandLine.createProcess().waitFor()
}

fun createBuildActionsFromMakefile(project: Project, path: Path) {
  val makefile = path.resolve("Makefile")
  if (!makefile.exists()) return
  val makefileText = makefile.readText()
  val lines = makefileText.split("\n")
  val buildActions =
      lines
          .asSequence()
          .filter { !it.startsWith("#") }
          .filter { it.contains(":") }
          .filter { !it.startsWith(" ") && !it.startsWith("\t") }
          .filter { !it.startsWith(".") }
          .filter { !it.startsWith("_") }
          .map { it.split(":")[0] }
          .map { it.trim() }
          .filter { it != "PHONY" }
          .filter { it != "build-mainnet-reproducible" }
          .filter { it != "clean" }
          .filter { it != "compress-wasm" }
          .filter { it != "start-server" }
          .filter { it != "store-contract-local" }
          .filter { it.isNotEmpty() }
          .sorted()
          .distinct()
          .toList()
  val runManager = RunManager.getInstance(project)
  buildActions.forEach {
    val config =
        runManager.createConfiguration(
            it,
            SecretNetworkContractConfigurationFactory(SecretNetworkContractConfigurationType()),
        )
    runManager.addConfiguration(config)
  }
  val buildIndex = buildActions.indexOf("build")
  if (buildIndex != -1) {
    val buildConfig = runManager.allSettings[buildIndex]
    runManager.selectedConfiguration = buildConfig
  } else {
    val buildMainnetIndex = buildActions.indexOf("build-mainnet")
    if (buildMainnetIndex != -1) {
      val buildMainnetConfig = runManager.allSettings[buildMainnetIndex]
      runManager.selectedConfiguration = buildMainnetConfig
    } else runManager.selectedConfiguration = runManager.allSettings.first()
  }

  print(buildActions)
}

/**
 * Updates the Cargo.toml file in the specified path.
 *
 * @param path the path to the project
 * @param projectName the name of the project
 */
fun updateCargoToml(path: Path, projectName: String) {
  replaceRegexInFile(
      path.resolve("Cargo.toml"),
      Regex("name = \"[^\"]*\""),
      "name = \"$projectName\""
  )
  replaceRegexInFile(
      path.resolve("Cargo.toml"),
      Regex("version = \"[^\"]*\""),
      "version = \"0.1.0\""
  )
  replaceRegexInFile(path.resolve("Cargo.toml"), Regex("authors = \\[[^]]*]"), "authors = []")
}

/**
 * Takes a single folder from a repository and creates a new project in the chosen path. shell
 * script equivalent:
 * ```sh
 * tmpDir=$(mktemp -d)
 * git clone $repo $tmpDir
 * pushd $tmpDir/$subFolder
 * find . -name . -o exec sh -c 'mv -- "$@" "$0"' $path {} + -type d -prune
 * popd
 * rm -rf $tmpDir
 * ```
 *
 * @param repo the repository to clone
 * @param subFolder the subfolder where the project's files are located
 * @param path the path to clone to
 * @param projectName the name of the project
 */
fun createProjectFromSubFolderInRepo(
    repo: String,
    subFolder: String,
    path: Path,
    projectName: String
) {
  val tmpPath = getTmpDir()
  cloneRepo(repo, tmpPath, projectName, false)
  updateCargoToml(tmpPath.resolve(subFolder), projectName)
  moveAllFilesFromFolderToFolder(tmpPath.resolve(subFolder), path)
  quickDelete(tmpPath.toString())
}

fun getTmpDir(): Path {
  val tmpFileName = Instant.now().toString()
  return Paths.get("/tmp/$tmpFileName")
}

/**
 * Moves all files in the specified path to the specified destination.
 *
 * @param from the path to move files from
 * @param to the path to move files to
 */
fun moveAllFilesFromFolderToFolder(from: Path, to: Path) {
  val commandLine = GeneralCommandLine()
  commandLine.workDirectory = from.toFile()
  commandLine.exePath = "find"
  commandLine.addParameter(".")
  commandLine.addParameter("-name")
  commandLine.addParameter(".")
  commandLine.addParameter("-o")
  commandLine.addParameter("-exec")
  commandLine.addParameter("sh")
  commandLine.addParameter("-c")
  commandLine.addParameter("mv -- \"\$@\" \"\$0\"")
  commandLine.addParameter(to.toString())
  commandLine.addParameter("{}")
  commandLine.addParameter("+")
  commandLine.addParameter("-type")
  commandLine.addParameter("d")
  commandLine.addParameter("-prune")
  commandLine.createProcess().waitFor()
}

/**
 * Creates a project using the cargo generate command
 *
 * @param repo the repository to clone
 * @param path the path to clone to
 */
fun createProjectUsingCargoGenerate(repo: String, path: Path) {
  val tmpPath = getTmpDir()
  val commandLine = GeneralCommandLine()
  commandLine.exePath = "cargo"
  commandLine.addParameter("generate")
  commandLine.addParameter("--bin")
  commandLine.addParameter("--git")
  commandLine.addParameter(repo.replace("https://github.com/", "git@github.com:"))
  commandLine.addParameter("--force")
  commandLine.addParameter("--name")
  commandLine.addParameter(tmpPath.toString())
  commandLine.createProcess().waitFor()
  moveAllFilesFromFolderToFolder(tmpPath, path)
  quickDelete(path.resolve(".git").toString())
  gitInit()
}

/**
 * Replaces a string in a file using sed
 *
 * @param path the path to the file
 * @param regex the regex to replace
 * @param replacement the replacement string
 */
fun replaceRegexInFile(path: Path, regex: Regex, replacement: String) {
  val content = path.readText()
  val newContent = regex.replaceFirst(content, replacement)
  path.write(newContent)
}
