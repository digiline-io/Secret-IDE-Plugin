package io.digiline.secretideplugin

import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.JBUI
import javax.swing.JPanel

class SecretNetworkSNIP721ContractProjectCreationWizard : JPanel() {
  val collectionName = JBTextField()
  val collectionSymbol = JBTextField()
  val royaltyPercentage = JBTextField()
  val royaltyAddress = JBTextField()
  val price = JBTextField()
  val dataFolder = TextFieldWithBrowseButton()
  val tokenSupplyPrivate = JBCheckBox()
  val ownerPublic = JBCheckBox()
  val metadataSealed = JBCheckBox()
  val unwrappedMetadataPublic = JBCheckBox()
  val minterCanUpdateMetadata = JBCheckBox()
  val ownerCanUpdateMetadata = JBCheckBox()
  val burnable = JBCheckBox()

  init {
    initComponents()
  }

  private fun initComponents() {
    layout = GridLayoutManager(1, 1, JBUI.emptyInsets(), -1, -1)
    val ui = panel {
      row("Collection Name") {
        collectionName()
      }
      row("Collection Symbol") {
        collectionSymbol()
      }
      row("Royalty Percentage") {
        royaltyPercentage()
      }
      row("Royalty Address") {
        royaltyAddress()
      }
      row("Buy using: sSCRT (more options coming soon)") {}
      row("Price") {
        price()
      }
      row("Data Folder") {
        dataFolder()
      }
      row("Is token supply public?") {
        tokenSupplyPrivate()
      }
      row("Is the owner public?") {
        ownerPublic()
      }
      row("Is the metadata sealed?") {
        metadataSealed()
      }
      row("Is unwrapped metadata public?") {
        unwrappedMetadataPublic()
      }
      row("Can the minter update the metadata?") {
        minterCanUpdateMetadata()
      }
      row("Can the owner update the metadata?") {
        ownerCanUpdateMetadata()
      }
      row("Is burnable?") {
        burnable()
      }
    }
    add(
      ui, GridConstraints(
        0, 0, 1, 1,
        GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH,
        GridConstraints.SIZEPOLICY_CAN_GROW,
        GridConstraints.SIZEPOLICY_CAN_GROW,
        null, null, null
      )
    )
//    scrollPane1!!.setViewportView(contractTemplate)
//    add(
//      scrollPane1, GridConstraints(
//        1, 0, 1, 1,
//        GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH,
//        GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
//        GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
//        null, null, null
//      )
//    )
  }
}