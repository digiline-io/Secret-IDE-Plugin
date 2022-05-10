package io.digiline.secretideplugin

import com.intellij.execution.Executor
import com.intellij.execution.ProgramRunnerUtil
import com.intellij.execution.RunManagerEx
import com.intellij.execution.RunnerAndConfigurationSettings
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.execution.executors.DefaultRunExecutor
import com.intellij.execution.runners.ProgramRunner
import java.io.File
import java.nio.file.Path

data class SecretNetworkContractCommandLine(
  val command: String,
  val additionalArguments: List<String> = emptyList(),
  val redirectInputFrom: File? = null,
  val environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
)