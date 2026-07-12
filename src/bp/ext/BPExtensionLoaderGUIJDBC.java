package bp.ext;

import bp.context.BPFileContext;
import bp.locale.BPLocaleHelpers;
import bp.project.BPResourceProjectJDBC;
import bp.ui.actions.BPActionHelperJDBC;
import bp.ui.view.BPProjectOverviewCompFactoryCommon;
import bp.ui.view.BPProjectOverviewManager;

public class BPExtensionLoaderGUIJDBC implements BPExtensionLoaderGUISwing
{
	public String getName()
	{
		return "JDBC GUI-Swing";
	}

	public String[] getParentExts()
	{
		return new String[] { "GUI-Swing", "JDBC" };
	}

	public String[] getDependencies()
	{
		return null;
	}

	public void install(BPFileContext context)
	{
		BPLocaleHelpers.registerHelper(new BPActionHelperJDBC());
	}

	public void preload()
	{
		BPProjectOverviewManager.register(BPResourceProjectJDBC.PRJTYPE_JDBC, new BPProjectOverviewCompFactoryCommon());
	}
}
