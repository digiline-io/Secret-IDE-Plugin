package io.digiline.secretideplugin

import com.intellij.ide.actions.runAnything.RunAnythingContext.BrowseRecentDirectoryContext.label
import com.intellij.openapi.ui.DialogWrapper
import org.jetbrains.annotations.Nullable
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class InstantiateContractDialog : DialogWrapper(true) {

  init {
    title = "Test DialogWrapper";
    init();
  }

  @Nullable
  @Override
  override fun createCenterPanel(): JComponent {
    val dialogPanel : JPanel = JPanel(BorderLayout());
    val label : JLabel = JLabel("testing");
    label.preferredSize = Dimension(100, 100);
    dialogPanel.add(label, BorderLayout.CENTER);

    return dialogPanel;
  }
}