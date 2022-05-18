package io.digiline.secretideplugin

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import javax.swing.JComponent
import com.intellij.ide.util.projectWizard.ModuleBuilder.ModuleConfigurationUpdater
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextField

class SecretIDESNIP721ModuleWizardStep(
  private val parent: SecretIDESNIP721ModuleBuilder,
  private val context: WizardContext,
  private val configurationUpdaterConsumer: ((ModuleConfigurationUpdater) -> Unit)? = null
) : ModuleWizardStep() {
  private val component = SecretNetworkSNIP721ContractProjectCreationWizard()

  override fun getComponent(): JComponent {
    return component
  }

  override fun validate(): Boolean {
    if(component.collectionName.text == "") return false
    if(component.collectionSymbol.text == "") return false
    if(component.royaltyPercentage.text == "") return false
    if(component.royaltyPercentage.text.toInt() < 1) return false
    if(component.royaltyPercentage.text.toInt() > 100) return false
    if(component.royaltyAddress.text == "") return false
    if(component.price.text == "") return false
    if(component.price.text.toInt() < 1) return false
    if(component.dataFolder.text == "") return false
    parent.data = component
    return true
  }

  override fun updateDataModel() {}
}
