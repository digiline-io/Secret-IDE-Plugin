package io.digiline.secretideplugin

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.AnsiEscapeDecoder
import com.intellij.execution.process.KillableProcessHandler
import com.intellij.openapi.util.Key

class SecretContractProcessHandler(
  commandLine: GeneralCommandLine,
  processColors: Boolean = true
) : KillableProcessHandler(commandLine), AnsiEscapeDecoder.ColoredTextAcceptor {
  private val decoder: AnsiEscapeDecoder? = if (processColors) SecretNetworkContractAnsiEscapeDecoder() else null

  init {
    setShouldDestroyProcessRecursively(true)
  }

  override fun notifyTextAvailable(text: String, outputType: Key<*>) {
    decoder?.escapeText(text, outputType, this) ?: super.notifyTextAvailable(text, outputType)
  }

  override fun coloredTextAvailable(text: String, attributes: Key<*>) {
    super.notifyTextAvailable(text, attributes)
  }

  override fun notifyProcessTerminated(exitCode: Int) {
    super.notifyProcessTerminated(exitCode)
    val settings = Settings.getInstance()
  }
}