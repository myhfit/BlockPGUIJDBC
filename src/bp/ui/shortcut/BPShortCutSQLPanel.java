package bp.ui.shortcut;

import java.util.ArrayList;
import java.util.List;

import bp.BPCore;
import bp.BPGUICore;
import bp.config.BPSetting;
import bp.config.BPSettingBase;
import bp.config.BPSettingItem;
import bp.project.BPResourceProject;
import bp.project.BPResourceProjectJDBC;
import bp.res.BPResource;
import bp.res.BPResourceJDBCLink;
import bp.res.BPResourceFileLocal;

public class BPShortCutSQLPanel extends BPShortCutBase
{
	public boolean run()
	{
		String[] params = m_params;
		String filename = "untitled.sql";
		BPResourceJDBCLink link = null;
		if (params.length > 1)
		{
			String projectname = params[0];
			String jlname = params[1];
			BPResourceProjectJDBC prj = (BPResourceProjectJDBC) BPCore.getProjectsContext().getProjectByName(projectname);
			if (prj != null)
			{
				BPResource res = new BPResourceFileLocal(prj.getDir().getFileFullName(), jlname);
				link = (BPResourceJDBCLink) prj.wrapResource(res);
			}
		}
		BPResourceJDBCLink plink = link;
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

		rc.addItem(BPSettingItem.create("projectname", "Project(JDBC) Name", BPSettingItem.ITEM_TYPE_SELECT, prjnames.toArray(new String[prjnames.size()])).setRequired(true));
		rc.addItem(BPSettingItem.create("jdbclinkfilename", "JDBC Link File", BPSettingItem.ITEM_TYPE_TEXT, null));

		rc.set("projectname", getParamValue(0));
		rc.set("jdbclinkfilename", getParamValue(1));
		return rc;
	}

	public void setSetting(BPSetting setting)
	{
		super.setSetting(setting);
		String prjname = setting.get("projectname");
		if (prjname == null)
			prjname = "";
		String jlfilename = setting.get("jdbclinkfilename");
		if (jlfilename == null)
			jlfilename = "";
		m_params = new String[] { prjname, jlfilename };
	}
}
