package bp.ui.shortcut;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import bp.BPCore;
import bp.BPGUICore;
import bp.config.BPSetting;
import bp.config.BPSettingBase;
import bp.config.BPSettingItem;
import bp.project.BPResourceProject;
import bp.project.BPResourceProjectJDBC;
import bp.res.BPResource;
import bp.res.BPResourceFileLocal;
import bp.res.BPResourceJDBCLink;
import bp.ui.util.CommonUIOperations;
import bp.util.TextUtil;

public class BPShortCutSQLPanel extends BPShortCutBase
{
	public final static String SCKEY_SQLPANEL = "SQL Panel";
	protected final static String SC_KEY_PRJNAME = "projectname";
	protected final static String SC_KEY_JLFN = "jdbclinkfilename";
	protected final static String SC_KEY_NEWWINDOW = "newwin";

	public boolean run()
	{
		String filename = "untitled.sql";
		BPResourceJDBCLink link = null;
		String projectname = TextUtil.eds(getParam(SC_KEY_PRJNAME));
		String jlname = TextUtil.eds(getParam(SC_KEY_JLFN));
		boolean newwin = "true".equals(getParam(SC_KEY_NEWWINDOW, "false"));
		if (projectname != null && jlname != null)
		{
			BPResourceProjectJDBC prj = (BPResourceProjectJDBC) BPCore.getProjectsContext().getProjectByName(projectname);
			if (prj != null)
			{
				BPResource res = new BPResourceFileLocal(prj.getDir().getFileFullName(), jlname);
				link = (BPResourceJDBCLink) prj.wrapResource(res);
			}
		}
		BPResourceJDBCLink plink = link;
		if (newwin || (!BPGUICore.execOnMainFrame(mf -> mf.isVisible())))
			CommonUIOperations.openFileNewWindow(filename, "SQL", "SQL Editor", null, plink);
		else
			BPGUICore.runOnMainFrame(mf -> mf.createEditorByFileSystem(filename, "SQL", "SQL Editor", null, plink));
		return true;
	}

	public BPSetting getSetting()
	{
		BPSettingBase rc = (BPSettingBase) super.getSetting();
		BPResourceProject[] prjs = BPCore.getProjectsContext().listProject();
		List<String> prjnames = new ArrayList<String>();
		for (BPResourceProject prj : prjs)
		{
			if (prj != null && prj instanceof BPResourceProjectJDBC)
			{
				prjnames.add(prj.getName());
			}
		}

		rc.addItem(BPSettingItem.create(SC_KEY_PRJNAME, "Project(JDBC) Name", BPSettingItem.ITEM_TYPE_SELECT, prjnames.toArray(new String[prjnames.size()])).setRequired(true));
		rc.addItem(BPSettingItem.create(SC_KEY_JLFN, "JDBC Link File", BPSettingItem.ITEM_TYPE_TEXT, null));
		rc.addItem(BPSettingItem.create(SC_KEY_NEWWINDOW, "New Window", BPSettingItem.ITEM_TYPE_SELECT, new String[] { "true", "false" }));

		rc.setAll(m_params);
		return rc;
	}

	public void setSetting(BPSetting setting)
	{
		super.setSetting(setting);
		m_params = setParamsFromSetting(new LinkedHashMap<String, Object>(), setting, true, false, SC_KEY_PRJNAME, SC_KEY_JLFN, SC_KEY_NEWWINDOW);
	}

	public String getShortCutKey()
	{
		return SCKEY_SQLPANEL;
	}

	protected String[] getParamKeys()
	{
		return new String[] { SC_KEY_PRJNAME, SC_KEY_JLFN, SC_KEY_NEWWINDOW };
	}
}
