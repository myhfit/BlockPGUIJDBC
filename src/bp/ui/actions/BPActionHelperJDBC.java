package bp.ui.actions;

import java.util.Map;

import bp.ui.res.icon.BPIconResV;

public class BPActionHelperJDBC extends BPActionHelperBase<BPActionConstJDBC>
{
	public final static String ACTIONHELPER_PACK_JDBC = "ac_jdbc";

	public String getPackName()
	{
		return ACTIONHELPER_PACK_JDBC;
	}

	public void initDefaults(Map<Integer, Object> actmap)
	{
		putAction(actmap, BPActionConstJDBC.TXT_STOPONPAGE, "Stop On Page", null, null, null, null);
		putAction(actmap, BPActionConstJDBC.ACT_BTNCONNECT, "Connect", "Connect", BPIconResV::CONNECT, "F2", null);
		putAction(actmap, BPActionConstJDBC.ACT_BTNDISCONNECT, "Disconnect", "Disconnect", BPIconResV::DISCONNECT, "F3", null);
		putAction(actmap, BPActionConstJDBC.ACT_BTNRUNPARAMED, "Run with parameters", "Run with parameters", BPIconResV::START, null, null);
		putAction(actmap, BPActionConstJDBC.ACT_BTNRESUME, "Resume", "Resume", BPIconResV::RESUME, null, null);
		putAction(actmap, BPActionConstJDBC.ACT_BTNRESUMETOEND, "Resume To End", "Resume To End", BPIconResV::TOEND, null, null);
		putAction(actmap, BPActionConstJDBC.ACT_BTNCOMMIT, "Commit", "Commit", BPIconResV::SAVE, null, null);
		putAction(actmap, BPActionConstJDBC.ACT_BTNROLLBACK, "Rollback", "Rollback", BPIconResV::NOTSAVE, null, null);
	}

	protected Class<BPActionConstJDBC> getConstClass()
	{
		return BPActionConstJDBC.class;
	}
}