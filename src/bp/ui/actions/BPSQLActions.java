package bp.ui.actions;

import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;

import bp.ui.editor.BPSQLPanel;
import bp.ui.res.icon.BPIconResV;

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

	protected BPSQLPanel m_editor;

	public BPSQLActions(BPSQLPanel editor)
	{
		m_editor = editor;
		actconn = BPAction.build("Connect").callback((e) -> m_editor.connect()).vIcon(BPIconResV.CONNECT()).acceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0)).tooltip("Connect(F2)").getAction();
		actdisconn = BPAction.build("Disconnect").callback((e) -> m_editor.disconnect()).vIcon(BPIconResV.DISCONNECT()).acceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)).tooltip("Disconnect(F3)").getAction();
		actrun = BPAction.build("Run").callback((e) -> m_editor.run()).vIcon(BPIconResV.START()).acceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0)).tooltip("Run(F5)").getAction();
		actrunparams = BPAction.build("Run with parameters").callback((e) -> m_editor.runWithParams()).vIcon(BPIconResV.START()).tooltip("Run with parameters").getAction();
		actresume = BPAction.build("Resume").callback((e) -> m_editor.resume()).vIcon(BPIconResV.RESUME()).tooltip("Resume").getAction();
		actresumetoend = BPAction.build("Resume To End").callback((e) -> m_editor.resumeToEnd()).vIcon(BPIconResV.TOEND()).tooltip("Resume to End").getAction();
		actstop = BPAction.build("Stop").callback((e) -> m_editor.stop()).vIcon(BPIconResV.STOP()).tooltip("Stop").getAction();
		actcommit = BPAction.build("Commit").callback((e) -> m_editor.commit()).vIcon(BPIconResV.SAVE()).tooltip("Commit").getAction();
		actrollback = BPAction.build("Rollback").callback((e) -> m_editor.rollback()).vIcon(BPIconResV.NOTSAVE()).tooltip("Rollback").getAction();
		actclone = BPAction.build("Clone").callback(m_editor::showClone).vIcon(BPIconResV.CLONE()).tooltip("Clone Data").getAction();
	}

	public Action[] getActions()
	{
		return new Action[] { actconn, actdisconn, BPAction.separator(), BPAction.separator(), actrun, actrunparams, actresume, actresumetoend, actstop, BPAction.separator(), BPAction.separator(), actcommit, actrollback, BPAction.separator(),
				BPAction.separator(), actclone };
	}
}