package io.digiline.secretideplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.dialog
import com.intellij.ui.layout.panel
import org.jetbrains.plugins.terminal.ShellTerminalWidget.getProcessTtyConnector
import org.jetbrains.plugins.terminal.TerminalView
import java.io.File
import java.io.IOException
import java.lang.Math.min
import javax.swing.JPasswordField
import javax.swing.JTextField


class BuildAndDeployPanel(private val project: Project, private val toolWindow: ToolWindow) :
  SimpleToolWindowPanel(true, false) {

  private val seed = JTextField()

  init {
    setContent(render())
  }

  private fun deploy() {
    val terminalView: TerminalView = TerminalView.getInstance(project)
    try {
      val shell = terminalView.createLocalShellWidget(project.basePath, "Deployment")
      val contractFile = File(project.basePath + "/contract.wasm")
      if (!contractFile.exists()) {
        Messages.showMessageDialog(project, "Contract file not found", "Deployment", Messages.getErrorIcon())
        return
      }
      val gas = (contractFile.length() * 6).coerceAtMost(6_000_000)
      shell.executeCommand("clear")
      shell.executeCommand("secretcli config chain-id pulsar-2")
      shell.executeCommand("secretcli config output json")
      shell.executeCommand("secretcli config node http://rpc.pulsar.griptapejs.com:26657")
      shell.executeCommand("secretcli config keyring-backend test")
      shell.executeCommand("secretcli config broadcast-mode block")
      shell.executeCommand("echo \"y\" | secretcli keys delete SecretIDE-Deployment")
      shell.executeCommand("echo \"${seed.text}\" | secretcli keys add SecretIDE-Deployment --recover")
      shell.executeCommand("clear")
      shell.executeCommand("codeId=$(echo \"y\" | secretcli tx compute store contract.wasm.gz --from SecretIDE-Deployment --gas $gas | jq '.logs[0].events[0].attributes[3].value')")
      shell.executeCommand("echo \"Contract stored successfully! Code ID: \$codeId\"")
    } catch (err: IOException) {
      err.printStackTrace()
    }
  }

  private fun render(): DialogPanel = panel {
    row {
      label("Contract Deployment")
    }
    row("Wallet Seed:") {
      seed()
    }
    row("") {
      label("Please don't use your main wallet seed.")
    }
    row("Deploy") {
      button("Deploy") {
        deploy()
      }
    }
  }
}