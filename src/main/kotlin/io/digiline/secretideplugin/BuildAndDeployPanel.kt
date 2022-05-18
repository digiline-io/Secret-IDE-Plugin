package io.digiline.secretideplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.separatorAndComment
import org.jetbrains.plugins.terminal.TerminalView
import java.io.File
import java.io.IOException
import javax.swing.*

class BuildAndDeployPanel(private val project: Project, private val toolWindow: ToolWindow) :
  SimpleToolWindowPanel(true, false) {
  private val codeIdModel = SpinnerNumberModel(0, 0, 99_999, 1)
  private val codeID : JSpinner = JSpinner(codeIdModel)
  private val label : JTextField = JTextField()
  private val message : JTextArea = JTextArea()

  private val seed = JPasswordField()
  private val networkSelector = ComboBox(arrayOf("Pulsar-2 Testnet", "Secret-4 Mainnet"))

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
      val seedFile = File(System.getProperty("user.home") + "/.Secret-IDE-seed")
      if(seedFile.exists())
        seedFile.delete()
      seedFile.writeText(String(seed.password)+"\n")
      shell.executeCommand("clear")
      if(networkSelector.selectedItem == "Pulsar-2 Testnet") {
        shell.executeCommand("secretcli config chain-id pulsar-2")
        shell.executeCommand("secretcli config node http://rpc.pulsar.griptapejs.com:26657")
      } else {
        shell.executeCommand("secretcli config chain-id secret-4")
        shell.executeCommand("secretcli config node https://scrt-validator.digiline.io:26657")
      }
      shell.executeCommand("secretcli config output json")
      shell.executeCommand("secretcli config keyring-backend test")
      shell.executeCommand("secretcli config broadcast-mode block")
      shell.executeCommand("echo \"y\" | secretcli keys delete SecretIDE-Deployment")
      shell.executeCommand("cat ~/.Secret-IDE-seed | secretcli keys add SecretIDE-Deployment --recover || exit 1")
      shell.executeCommand("clear")
      shell.executeCommand("codeId=$(echo \"y\" | secretcli tx compute store contract.wasm.gz --from SecretIDE-Deployment --gas $gas | jq '.logs[0].events[0].attributes[3].value')")
      shell.executeCommand("echo \"Contract stored successfully! Code ID: \$codeId\"")
    } catch (err: IOException) {
      err.printStackTrace()
    }
  }

  private fun render(): DialogPanel {
    message.rows = 20
    message.lineWrap = true
    return panel {
      row {
        label("Deploy Contract")
      }
      separatorAndComment()
      row("Wallet Seed:") {
        seed()
      }
      row("") {
        label("Please don't use your main wallet seed.")
      }
      row("Deploy to:") {
        networkSelector()
      }
      row("Deploy") {
        button("Deploy") {
          deploy()
        }
      }
      row("Instantiate Contract") {}
      separatorAndComment()
      row("Code ID:") {
        codeID()
      }
      row("Label:") {
        label()
      }
      row("Message:") {
        message()
      }
      row {
        button("Instantiate") {
          val jsonWithQuotes = message.text
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\$", "\\\$")
          val command = "secretcli tx compute instantiate " +
              codeID.value + " \"${jsonWithQuotes}\" --label '${label.text}'" +
              "--from 'SecretIDE-Deployment'"
          val terminalView: TerminalView = TerminalView.getInstance(project)
          try {
            val shell = terminalView.createLocalShellWidget(project.basePath, "Instantiation")
            shell.executeCommand("clear")
            shell.executeCommand(command)
            shell.handleAnyKeyPressed()
            shell.close()
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
      }
    }
  }
}