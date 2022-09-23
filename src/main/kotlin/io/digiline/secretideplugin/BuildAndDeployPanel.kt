package io.digiline.secretideplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.separatorAndComment
import java.io.File
import java.io.IOException
import javax.swing.*
import org.jetbrains.plugins.terminal.TerminalView

class BuildAndDeployPanel(private val project: Project) : SimpleToolWindowPanel(true, false) {
  private val codeIdModel = SpinnerNumberModel(0, 0, 99_999, 1)
  private val codeID: JSpinner = JSpinner(codeIdModel)
  private val label: JTextField = JTextField()
  private val gasForInstantiationModel = SpinnerNumberModel(200_000, 100_000, 6_000_000, 100)
  private val gasForInstantiation: JSpinner = JSpinner(gasForInstantiationModel)
  private val message: JTextArea = JTextArea()

  private val seed = JPasswordField()
  private val networkSelector = ComboBox(arrayOf("Pulsar-2 Testnet", "Secret-4 Mainnet"))

  init {
    setContent(render())
  }

  private fun deploy() {
    val terminalView: TerminalView = TerminalView.getInstance(project)
    try {
      val contractFile = File(project.basePath + "/contract.wasm")
      if (!contractFile.exists()) {
        Messages.showMessageDialog(
            project,
            "Contract file not found",
            "Deployment",
            Messages.getErrorIcon()
        )
        return
      }

      val gas = (contractFile.length() * 7).coerceAtMost(6_000_000)
      val seedFile = File(System.getProperty("user.home") + "/.Secret-IDE-seed")
      if (seedFile.exists()) seedFile.delete()
      val shell = terminalView.createLocalShellWidget(project.basePath, "Deployment")
      seedFile.writeText(String(seed.password) + "\n")
      shell.executeCommand("clear")
      if (networkSelector.selectedItem == "Pulsar-2 Testnet") {
        shell.executeCommand("secretcli config chain-id pulsar-2")
        shell.executeCommand("secretcli config node https://rpc.pulsar.scrttestnet.com:443")
      } else {
        shell.executeCommand("secretcli config chain-id secret-4")
        shell.executeCommand("secretcli config node https://scrt-validator.digiline.io:26657")
      }
      shell.executeCommand("secretcli config output json")
      shell.executeCommand("secretcli config keyring-backend test")
      shell.executeCommand("secretcli config broadcast-mode block")
      shell.executeCommand("secretcli keys delete SecretIDE-Deployment -y")
      shell.executeCommand(
          "cat ~/.Secret-IDE-seed | secretcli keys add SecretIDE-Deployment --recover || exit 1"
      )
      shell.executeCommand("clear")
      shell.executeCommand(
          """codeId=$(secretcli tx compute store contract.wasm.gz --from SecretIDE-Deployment --gas $gas -y | jq '.logs[0].events[0].attributes[] | select(.key=="code_id").value"""
      )
      shell.executeCommand("echo \"Contract stored successfully! Code ID: \${'$'}codeId\"")
    } catch (err: IOException) {
      err.printStackTrace()
    }
  }

  private fun instantiate() {
    val jsonWithQuotes =
        message.text.replace("\"", "\\\"").replace("\n", "").replace("\r", "").replace("\$", "\\\$")
    val command =
        "secretcli tx compute instantiate " +
            codeID.value +
            " \"${jsonWithQuotes}\" --label '${label.text}'" +
            " --from 'SecretIDE-Deployment' --gas '${gasForInstantiation.value}' -y"
    val terminalView: TerminalView = TerminalView.getInstance(project)
    try {
      val shell = terminalView.createLocalShellWidget(project.basePath, "Instantiation")
      shell.executeCommand("clear")
      shell.executeCommand(command)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  private fun render(): DialogPanel {
    message.rows = 20
    message.lineWrap = true
    return panel {
      row { label("Deploy Contract") }
      separatorAndComment()
      row("Wallet Seed:") { seed() }
      row("") { label("Please don't use your main wallet seed.") }
      row("Deploy to:") { networkSelector() }
      row("Deploy") { button("Deploy") { deploy() } }
      row("Instantiate Contract") {}
      separatorAndComment()
      row("Code ID:") { codeID() }
      row("Label:") { label() }
      row("Input:") { message() }
      row("Gas:") { gasForInstantiation() }
      row { button("Instantiate") { instantiate() } }
    }
  }
}
