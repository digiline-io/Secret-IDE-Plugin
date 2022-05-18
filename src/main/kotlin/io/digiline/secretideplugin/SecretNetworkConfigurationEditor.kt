package io.digiline.secretideplugin

import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.ui.components.Label
import com.intellij.ui.layout.CCFlags
import com.intellij.ui.layout.LayoutBuilder
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.panel
import javax.swing.JComponent
import javax.swing.JTextField

class SecretNetworkConfigurationEditor(project: Project) : SettingsEditor<SecretNetworkContractConfiguration>() {
  private val command: JTextField = JTextField()

  override fun resetEditorFrom(configuration: SecretNetworkContractConfiguration) {
    command.text = configuration.commandConfiguration.command
  }

  override fun applyEditorTo(configuration: SecretNetworkContractConfiguration) {
    configuration.commandConfiguration.command = command.text

  }

  override fun createEditor(): JComponent = panel {
    labeledRow("Makefile &Command:", command) {
      command(CCFlags.pushX, CCFlags.growX)
    }
  }

  private fun LayoutBuilder.labeledRow(labelText: String, component: JComponent, init: Row.() -> Unit) {
    val label = Label(labelText)
    label.labelFor = component
    row(label) { init() }
  }
}