package io.digiline.secretideplugin

import Icons.SdkIcons
import com.intellij.ui.IdeBorderFactory
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import com.moandjiezana.toml.Toml
import java.awt.Dimension
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse.BodyHandlers
import java.util.*
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.SwingConstants


class SecretNetworkContractProjectCreationWizard : JPanel() {
  private var contractTemplateLabel: JLabel? = null
  private var scrollPane1: JBScrollPane? = null
  var contractTemplate: JBList<ContractTemplate>? = null

  init {
    createUIComponents()
    initComponents()
  }

  private fun createUIComponents() {
    val uri: URI =
      URI.create("https://raw.githubusercontent.com/zorostang/secret-network-community-code-repositories/main/repos.toml")
    val request: HttpRequest = HttpRequest.newBuilder(uri).build()
    val content: String = HttpClient.newHttpClient().send(request, BodyHandlers.ofString()).body()
    val parsedContent = Toml().read(content).toMap()
    val availableRepos = (parsedContent["repo"] as List<HashMap<String, *>>).map {
      return@map ContractTemplate(
        type = if (it.containsKey("use-cargo-generate") && it["use-cargo-generate"] as Boolean) RepoType.cargoGenerate else RepoType.url,
        url = it["url"] as String,
        title = it["title"] as String,
        subfolder = if (it.containsKey("subfolder")) it["subfolder"] as String else ""
      )
    }
    contractTemplate = JBList(availableRepos)
    contractTemplate!!.selectedIndex = 0
  }

  private fun initComponents() {
    contractTemplate!!.installCellRenderer {
      val label = JBLabel(it.title, SdkIcons.Sdk_default_icon, SwingConstants.LEFT)
      label.border = IdeBorderFactory.createEmptyBorder(2, 4, 2, 4)
      label
    }

    val bundle = ResourceBundle.getBundle("messages.MyBundle")
    contractTemplateLabel = JLabel()
    scrollPane1 = JBScrollPane()
    layout = GridLayoutManager(2, 1, JBUI.emptyInsets(), -1, -1)
    contractTemplateLabel!!.text = "Contract Template:"
    add(
      contractTemplateLabel, GridConstraints(
        0, 0, 1, 1,
        GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE,
        GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK,
        null, null, Dimension(200, 10)
      )
    )
    scrollPane1!!.setViewportView(contractTemplate)
    add(
      scrollPane1, GridConstraints(
        1, 0, 1, 1,
        GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
        GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
        null, null, null
      )
    )
  }
}