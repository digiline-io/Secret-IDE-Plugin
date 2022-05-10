package io.digiline.secretideplugin

import com.intellij.execution.configurations.CommandLineState
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessTerminatedListener
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.psi.search.GlobalSearchScopes

class SecretNetworkContractRunState(
  environment: ExecutionEnvironment,
  private val runConfiguration: SecretNetworkContractConfiguration
) : CommandLineState(environment) {
  val project = environment.project

  init {
    val scope = GlobalSearchScopes.executionScope(environment.project, environment.runProfile)
    consoleBuilder = SecretNetworkContractConsoleBuilder(environment.project, scope)
  }

  override fun startProcess(): ProcessHandler {
    val commandLine = runConfiguration.toGeneralCommandLine(project)
    val handler = SecretContractProcessHandler(commandLine)
    ProcessTerminatedListener.attach(handler)
    return handler
  }
}