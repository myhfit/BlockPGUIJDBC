package bp.ui.actions;

import javax.swing.Action;

import bp.ui.editor.BPSQLPanel;

public class BPSQLActions
{
	public Action actconn;
	public Action actdisconn;
	public Action actrun;
	public Action actrunparams;
	public Action actresume;
	public Action actresumetoend;
	public Action actstop;
	public Action actcommit;
	public Action actrollback;
	public Action actclone;
	public Action actsetenv;

	protected BPSQLPanel m_editor;

	public BPSQLActions(BPSQLPanel editor)
	{
		m_editor = editor;
		actconn = BPActionHelpers.getAction(BPActionConstJDBC.ACT_BTNCONNECT, e -> m_editor.connect());
		actdisconn = BPActionHelpers.getAction(BPActionConstJDBC.ACT_BTNDISCONNECT, e -> m_editor.disconnect());
		actrun = BPActionHelpers.getActionWithAlias(BPActionConstCommon.ACT_BTNRUN, BPActionConstCommon.ACT_BTNRUN_ACC, e -> m_editor.run());
		actrunparams = BPActionHelpers.getAction(BPActionConstJDBC.ACT_BTNRUNPARAMED, e -> m_editor.runWithParams());
		actresume = BPActionHelpers.getAction(BPActionConstJDBC.ACT_BTNRESUME, e -> m_editor.resume());
		actresumetoend = BPActionHelpers.getAction(BPActionConstJDBC.ACT_BTNRESUMETOEND, e -> m_editor.resumeToEnd());
		actstop = BPActionHelpers.getAction(BPActionConstCommon.ACT_BTNSTOP, e -> m_editor.stop());
		actcommit = BPActionHelpers.getAction(BPActionConstJDBC.ACT_BTNCOMMIT, e -> m_editor.commit());
		actrollback = BPActionHelpers.getAction(BPActionConstJDBC.ACT_BTNROLLBACK, e -> m_editor.rollback());
		actclone = BPActionHelpers.getAction(BPActionConstCommon.ACT_BTNCLONE, m_editor::showClone);
		actsetenv = BPActionHelpers.getAction(BPActionConstCommon.ACT_BTNCONFIG, m_editor::updateEnv);
	}

	public Action[] getActions()
	{
		return new Action[] { actconn, actdisconn, BPAction.separator(), BPAction.separator(), actrun, actrunparams, actresume, actresumetoend, actstop, BPAction.separator(), BPAction.separator(), actcommit, actrollback, BPAction.separator(),
				BPAction.separator(), actclone, actsetenv };
	}
}