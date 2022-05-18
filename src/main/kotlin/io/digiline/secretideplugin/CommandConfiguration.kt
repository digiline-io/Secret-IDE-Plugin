package io.digiline.secretideplugin

import com.intellij.execution.configuration.EnvironmentVariablesData

data class CommandConfiguration(
  var command: String = "",
  var env: EnvironmentVariablesData = EnvironmentVariablesData.DEFAULT,
) {
  fun clone(): CommandConfiguration {
    val other = CommandConfiguration()
    other.command = command
    other.env = env
    return other
  }
}