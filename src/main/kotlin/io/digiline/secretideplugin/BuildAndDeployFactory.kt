package io.digiline.secretideplugin

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class BuildAndDeployFactory : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val buildAndDeployPanel = BuildAndDeployPanel(project)
    val contentFactory = ContentFactory.SERVICE.getInstance()
    val content = contentFactory.createContent(buildAndDeployPanel, "", false)
    toolWindow.contentManager.addContent(content)
  }
}