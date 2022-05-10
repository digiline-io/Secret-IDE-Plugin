package io.digiline.secretideplugin

import com.intellij.util.xmlb.annotations.MapAnnotation

@MapAnnotation(
  keyAttributeName = "run",
  valueAttributeName = "settings", entryTagName = "config"
)
class SNCRunConfigMap : HashMap<Int, SecretNetworkContractRunConfiguration>() {
  override fun get(key: Int): SecretNetworkContractRunConfiguration {
    val value = super.get(key)
    return if (value == null) {
      val default = SecretNetworkContractRunConfiguration()
      put(key, default)
      default
    } else {
      value
    }
  }
}