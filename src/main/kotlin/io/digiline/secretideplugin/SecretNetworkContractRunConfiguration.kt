package io.digiline.secretideplugin

data class SecretNetworkContractRunConfiguration(
  var command: String = "",
  var options: List<String> = listOf(),
  var env: Map<String, String> = mapOf(),
)