package io.digiline.secretideplugin

import com.intellij.ide.util.projectWizard.ModuleWizardStep
import javax.swing.JComponent
import com.intellij.ide.util.projectWizard.ModuleBuilder.ModuleConfigurationUpdater
import com.intellij.ide.util.projectWizard.WizardContext

class SecretIDEModuleWizardStep(
  private val parent: SecretIDEModuleBuilder,
  private val context: WizardContext,
  private val configurationUpdaterConsumer: ((ModuleConfigurationUpdater) -> Unit)? = null
) : ModuleWizardStep() {
  private val component = SecretNetworkContractProjectCreationWizard()

  override fun getComponent(): JComponent {
    return component
  }

  override fun validate(): Boolean {
    if (component.contractTemplate == null) return false
    val template = component.contractTemplate!!.selectedValue ?: return false;
    if (!super.validate()) return false
    parent.template = template
    return true
  }

  override fun updateDataModel() {}
}
