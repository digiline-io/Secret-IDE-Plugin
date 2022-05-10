package io.digiline.secretideplugin

import com.intellij.execution.filters.TextConsoleBuilderImpl
import com.intellij.execution.ui.ConsoleView
import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope

open class SecretNetworkContractConsoleBuilder(project: Project, scope: GlobalSearchScope) : TextConsoleBuilderImpl(project, scope) {
  override fun createConsole(): ConsoleView = SecretNetworkContractConsoleView(project, scope, isViewer, true)
}