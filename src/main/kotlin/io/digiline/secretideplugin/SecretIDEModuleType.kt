package io.digiline.secretideplugin

import Icons.SdkIcons
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import javax.swing.Icon

class SecretIDEModuleType : ModuleType<SecretIDEModuleBuilder>(ID) {
  override fun createModuleBuilder(): SecretIDEModuleBuilder {
    return SecretIDEModuleBuilder()
  }

  override fun getName(): String {
    return "Secret Network Contract"
  }

  override fun getDescription(): String {
    return "Secret Network contract"
  }

  override fun getNodeIcon(b: Boolean): Icon {
    return SdkIcons.Sdk_default_icon
  }

  override fun createWizardSteps(
    wizardContext: WizardContext,
    moduleBuilder: SecretIDEModuleBuilder,
    modulesProvider: ModulesProvider
  ): Array<ModuleWizardStep> {
    return super.createWizardSteps(wizardContext, moduleBuilder, modulesProvider)
  }

  companion object {
    private const val ID = "SCRT_NETWORK_MODULE_TYPE"
    val instance: SecretIDEModuleType
      get() = ModuleTypeManager.getInstance().findByID(ID) as SecretIDEModuleType
  }
}
