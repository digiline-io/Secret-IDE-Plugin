package io.digiline.secretideplugin

import cloneRepo
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.ConfigurationException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.util.Disposer
import createBuildActionsFromMakefile
import createProjectFromSubFolderInRepo
import createProjectUsingCargoGenerate
import java.nio.file.Paths

class SecretIDEModuleBuilder : ModuleBuilder() {
  var template: ContractTemplate? = null;

  override fun setupRootModel(model: ModifiableRootModel) {
    val root = doAddContentEntry(model)?.file ?: return
    model.inheritSdk()
    root.refresh(false, true)
    val project = model.project
    val name = project.name.replace(' ', '_')
    if (template != null) {
      object : Task.Modal(project, "Creating Project", true) {
        override fun run(indicator: ProgressIndicator) {
          indicator.isIndeterminate = true
          val template = template!!
          val path = Paths.get(project.basePath!!)
          when (template.type) {
            RepoType.url -> if (template.subfolder.isEmpty()) {
              cloneRepo(template.url, path, name)
            } else {
              createProjectFromSubFolderInRepo(template.url, template.subfolder, path, name)
            }
            RepoType.cargoGenerate -> createProjectUsingCargoGenerate(template.url, path)
          }
          createBuildActionsFromMakefile(project, path)
        }
      }.queue()
    }
  }

  @Suppress("DialogTitleCapitalization")
  override fun getModuleType(): SecretIDEModuleType {
    return SecretIDEModuleType.instance
  }

  override fun getCustomOptionsStep(context: WizardContext, parentDisposable: Disposable): ModuleWizardStep {
    return SecretIDEModuleWizardStep(this, context).apply {
      Disposer.register(parentDisposable, this::disposeUIResources)
    }
  }

  @Throws(ConfigurationException::class)
  override fun validateModuleName(moduleName: String): Boolean {
    val errorMessage = RustPackageNameValidator.validate(moduleName, true) ?: return true
    throw ConfigurationException(errorMessage)
  }
}
