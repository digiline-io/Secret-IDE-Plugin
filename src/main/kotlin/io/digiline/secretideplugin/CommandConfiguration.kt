package io.digiline.secretideplugin

import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.ide.scratch.ScratchFileService
import com.intellij.ide.scratch.ScratchRootType
import java.nio.file.Paths

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