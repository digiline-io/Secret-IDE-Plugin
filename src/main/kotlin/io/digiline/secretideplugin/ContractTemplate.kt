package io.digiline.secretideplugin

data class ContractTemplate(val type: RepoType, val url: String, val title: String, val subfolder: String);

enum class RepoType {
  url,
  cargoGenerate
}
