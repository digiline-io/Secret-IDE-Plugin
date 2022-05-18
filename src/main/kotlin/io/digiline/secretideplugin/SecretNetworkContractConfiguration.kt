package io.digiline.secretideplugin

import com.intellij.execution.Executor
import com.intellij.execution.configurations.*
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project

@Suppress("PrivatePropertyName")
class SecretNetworkContractConfiguration(
  project: Project,
  name: String?,
  factory: ConfigurationFactory
) : LocatableConfigurationBase<RunProfileState>(project, factory, name) {
  var commandConfiguration: CommandConfiguration = CommandConfiguration()

  override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
    return SecretNetworkContractRunState(environment, this);
  }

  override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> =
    SecretNetworkConfigurationEditor(project)

  fun setFromCmd(commandLine: SecretNetworkContractCommandLine) {
    commandConfiguration = CommandConfiguration(commandLine.command, commandLine.environmentVariables)
  }

  fun toGeneralCommandLine(
    project: Project
  ): GeneralCommandLine {
    val cmdExecutable = "make"

    val params = commandConfiguration.command.split(" ")

    val generalCommandLine = GeneralCommandLine(cmdExecutable)
      .withWorkDirectory(project.basePath)
      .withEnvironment("TERM", "ansi")
      .withParameters(params)
      .withCharset(Charsets.UTF_8)
      .withRedirectErrorStream(true)

    commandConfiguration.env.configureCommandLine(generalCommandLine, true)

    return generalCommandLine
  }
}