package com.github.thelmuxkriovar.secretideplugin.services

import com.intellij.openapi.project.Project
import com.github.thelmuxkriovar.secretideplugin.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
