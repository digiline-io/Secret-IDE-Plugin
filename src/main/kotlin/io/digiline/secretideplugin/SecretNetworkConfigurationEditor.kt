package io.digiline.secretideplugin

import com.intellij.execution.ExecutionBundle
import com.intellij.execution.configuration.EnvironmentVariablesComponent
import com.intellij.execution.configuration.EnvironmentVariablesData
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.util.SystemInfo
import com.intellij.ui.components.CheckBox
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