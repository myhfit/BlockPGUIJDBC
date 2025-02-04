package bp.ui.actions;

import java.awt.event.KeyEvent;

import javax.swing.Action;

import bp.BPGUICore;
import bp.res.BPResourceJDBCLink;
import bp.res.BPResourceFileLocal;
import bp.ui.scomp.BPTree;
import bp.ui.tree.BPPathTreePanel.BPEventUIPathTree;
import bp.ui.tree.BPTreeComponent;

public class BPProjectsTreeNodeActionsJDBC
{

	public BPProjectsTreeNodeActionsJDBC()
	{
	}

	public BPAction getNewAction(BPTreeComponent<BPTree> tree, BPResourceJDBCLink res, int channelid)
	{
		BPAction rc = BPAction.build("New").mnemonicKey(KeyEvent.VK_N).getAction();
		BPResourceFileLocal f = new BPResourceFileLocal("untitled.sql");
		BPAction actneweditor = BPAction.build("SQL Editor").callback((e) ->
		{
			BPGUICore.EVENTS_UI.trigger(channelid, BPEventUIPathTree.makeActionEvent(BPPathTreeNodeActions.ACTION_NEWFILEUNSAVED, f, res));
		}).mnemonicKey(KeyEvent.VK_S).getAction();
		rc.putValue(BPAction.SUB_ACTIONS, new Action[] { actneweditor });
		return rc;
	}
}
