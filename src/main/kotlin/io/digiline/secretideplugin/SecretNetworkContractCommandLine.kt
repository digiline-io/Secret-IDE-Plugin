package io.digiline.secretideplugin

import com.intellij.execution.configuration.EnvironmentVariablesData
import java.io.File

data class SecretNetworkContractCommandLine(
  val command: String,
  val additionalArguments: List<String> = emptyList(),
  val redirectInputFrom: File? = null,
  val environmentVariables: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT
)