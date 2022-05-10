package io.digiline.secretideplugin

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.CachedSingletonsRegistry
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.util.xmlb.XmlSerializerUtil
import com.intellij.util.xmlb.annotations.Tag

@Suppress("MemberVisibilityCanBePrivate")
@State(
  name = "SecretNetworkContract",
  storages = [
    Storage(StoragePathMacros.WORKSPACE_FILE),
    Storage("misc.xml", deprecated = true)
  ]
)
class Settings : PersistentStateComponent<Settings> {
  override fun getState(): Settings {
    return this
  }

  override fun loadState(state: Settings) {
    XmlSerializerUtil.copyBean(state, this)
  }

  companion object {
    private var ourInstance = CachedSingletonsRegistry.markCachedField(Settings::class.java)

    fun getInstance(): Settings {
      return ourInstance ?: run {
        val result = ApplicationManager.getApplication().getService(Settings::class.java)
        ourInstance = result
        result
      }
    }
  }

  @Tag("RunConfigurations")
  var runConfigurations: SNCRunConfigMap = SNCRunConfigMap()
}
