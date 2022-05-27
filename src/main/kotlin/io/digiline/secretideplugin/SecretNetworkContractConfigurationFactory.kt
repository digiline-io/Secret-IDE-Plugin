package io.digiline.secretideplugin

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.openapi.project.Project

class SecretNetworkContractConfigurationFactory(type: SecretNetworkContractConfigurationType) :
  ConfigurationFactory(type) {
  override fun getId(): String = ID

  override fun createTemplateConfiguration(project: Project): RunConfiguration {
    return SecretNetworkContractConfiguration(project, "Secret Network Contract", this)
  }

  companion object {
    const val ID: String = "SecretNetworkContract"
  }

  override fun createConfiguration(name: String?, template: RunConfiguration): RunConfiguration {
    val config = super.createConfiguration(name, template) as SecretNetworkContractConfiguration
    config.commandConfiguration = config.commandConfiguration.clone()
    config.commandConfiguration.command = name ?: "";
    return config
  }
}k