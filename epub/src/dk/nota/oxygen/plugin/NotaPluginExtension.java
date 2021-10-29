package dk.nota.oxygen.plugin;

import javax.swing.JComponent;
import javax.swing.JMenuBar;

import dk.nota.dtb.conversion.InspirationOutput;
import dk.nota.oxygen.actions.epub.ConcatAction;
import dk.nota.oxygen.actions.epub.DaisyComicAction;
import dk.nota.oxygen.actions.epub.EpubToDocxAction;
import dk.nota.oxygen.actions.epub.EpubToDtbAction;
import dk.nota.oxygen.actions.epub.ImportDocxAction;
import dk.nota.oxygen.actions.epub.InspirationOutputAction;
import dk.nota.oxygen.actions.epub.NavigationUpdateAction;
import dk.nota.oxygen.actions.epub.ReloadDocumentsAction;
import dk.nota.oxygen.actions.epub.SplitAction;
import dk.nota.oxygen.listeners.WorkspaceSetupListener;
import ro.sync.exml.plugin.workspace.WorkspaceAccessPluginExtension;
import ro.sync.exml.workspace.api.PluginWorkspaceProvider;
import ro.sync.exml.workspace.api.editor.WSEditor;
import ro.sync.exml.workspace.api.standalone.MenuBarCustomizer;
import ro.sync.exml.workspace.api.standalone.StandalonePluginWorkspace;
import ro.sync.exml.workspace.api.standalone.ToolbarComponentsCustomizer;
import ro.sync.exml.workspace.api.standalone.ToolbarInfo;
import ro.sync.exml.workspace.api.standalone.ui.Menu;
import ro.sync.exml.workspace.api.standalone.ui.ToolbarButton;

public class NotaPluginExtension implements WorkspaceAccessPluginExtension {
	
	public static final String DTB_TOOLBAR = "dk.nota.oxygen.epub.toolbar.dtb";
	public static final String NAV_TOOLBAR = "dk.nota.oxygen.epub.toolbar.nav";
	public static final String OPF_TOOLBAR = "dk.nota.oxygen.epub.toolbar.opf";
	public static final String TOOLBAR_PREFIX = "dk.nota.oxygen.epub";
	public static final String XHTML_TOOLBAR = "dk.nota.oxygen.epub.toolbar.xhtml";
	
	@Override
	public boolean applicationClosing() {
		return true;
	}

	@Override
	public void applicationStarted(StandalonePluginWorkspace pluginWorkspace) {
		pluginWorkspace.addEditorChangeListener(new WorkspaceSetupListener(
				pluginWorkspace), StandalonePluginWorkspace.MAIN_EDITING_AREA);
		pluginWorkspace.addToolbarComponentsCustomizer(
				new EpubToolbarCustomizer());
	}
	
	public static WSEditor getCurrentEditor() {
		return PluginWorkspaceProvider.getPluginWorkspace()
				.getCurrentEditorAccess(StandalonePluginWorkspace
						.MAIN_EDITING_AREA);
	}
	
	private class EpubToolbarCustomizer implements ToolbarComponentsCustomizer {
		
		@Override
		public void customizeToolbar(ToolbarInfo toolbar) {
			if (!toolbar.getToolbarID().startsWith(TOOLBAR_PREFIX)) return;
			switch (toolbar.getToolbarID()) {
			case NAV_TOOLBAR:
				setupNavToolbar(toolbar);
				return;
			case OPF_TOOLBAR:
				setupOpfToolbar(toolbar);
				return;
			case XHTML_TOOLBAR:
				setupXhtmlToolbar(toolbar);
			}
		}
		
		private void setupNavToolbar(ToolbarInfo toolbar) {
			JComponent[] navComponents = new JComponent[] {
				new ToolbarButton(new NavigationUpdateAction(), true)
			};
			toolbar.setTitle("EPUB Navigation");
			toolbar.setComponents(navComponents);
		}
		
		private void setupOpfToolbar(ToolbarInfo toolbar) {
			Menu contentMenu = new Menu("Content");
			contentMenu.add(new ConcatAction());
			contentMenu.add(new SplitAction());
			contentMenu.addSeparator();
			contentMenu.add(new ReloadDocumentsAction());
			Menu exportMenu = new Menu("Export");
			exportMenu.add(new EpubToDtbAction());
			exportMenu.addSeparator();
			exportMenu.add(new InspirationOutputAction(InspirationOutput
					.INSP_ETEXT));
			exportMenu.add(new InspirationOutputAction(InspirationOutput
					.INSP_PROOF));
			exportMenu.add(new InspirationOutputAction(InspirationOutput
					.INSP_AUDIO));
			exportMenu.add(new InspirationOutputAction(InspirationOutput
					.INSP_BRAILLE));
			exportMenu.add(new InspirationOutputAction(InspirationOutput
					.INSP_PRINT));
			exportMenu.addSeparator();
			exportMenu.add(new EpubToDocxAction());
			exportMenu.addSeparator();
			exportMenu.add(new DaisyComicAction());
			Menu importMenu = new Menu("Import");
			importMenu.add(new ImportDocxAction());
//			importMenu.addSeparator();
//			importMenu.add(new ImportDtbAction());
			JComponent[] opfComponents = new JComponent[] {
				contentMenu,
				importMenu,
				exportMenu
			};
			toolbar.setTitle("EPUB OPF");
			toolbar.setComponents(opfComponents);
		}
		
		private void setupXhtmlToolbar(ToolbarInfo toolbar) {
			Menu importMenu = new Menu("Import");
			importMenu.add(new ImportDocxAction());
			JComponent[] xhtmlComponents = new JComponent[] {
				new ToolbarButton(new NavigationUpdateAction(), true),
				importMenu
			};
			toolbar.setTitle("EPUB XHTML");
			toolbar.setComponents(xhtmlComponents);
		}
		
	}

}
