package nota.oxygen.epub.plugins;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import ro.sync.exml.workspace.api.standalone.MenuBarCustomizer;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;

public class SpellcheckMenuBarCustomizer implements MenuBarCustomizer {
	
	private StandalonePluginWorkspace workspace;

	public SpellcheckMenuBarCustomizer(StandalonePluginWorkspace workspace) {
		this.workspace = workspace;
	}

	@Override
	public void customizeMainMenu(JMenuBar menuBar) {
		
		if (menuBar.getMenuCount()>1)
		{
			JMenu editMenu = menuBar.getMenu(1);
			int insertIndex = editMenu.getItemCount();
			for (int i = 0; i < editMenu.getItemCount(); i++)
			{
				JMenuItem item = editMenu.getItem(i);
				if (item != null)
				{
					if (item.getText()=="Check Spelling...")
					{
						insertIndex = i+1;
						break;
					}
				}
			}
			editMenu.insert(new SpellcheckAction(workspace), insertIndex);
		}
	}

}
