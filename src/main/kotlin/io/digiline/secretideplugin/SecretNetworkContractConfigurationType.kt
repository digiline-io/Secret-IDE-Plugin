package io.digiline.secretideplugin

import Icons.SdkIcons
import com.intellij.execution.configurations.*

class SecretNetworkContractConfigurationType : ConfigurationTypeBase(
  "SecretNetworkContract",
  "Secret Network",
  "Secret Network contract",
  SdkIcons.Sdk_default_icon
) {
  init {
    addFactory(SecretNetworkContractConfigurationFactory(this))
  }

  val factory: ConfigurationFactory get() = configurationFactories.single()

  companion object {
    fun getInstance(): SecretNetworkContractConfigurationType =
      ConfigurationTypeUtil.findConfigurationType(SecretNetworkContractConfigurationType::class.java)
  }
}