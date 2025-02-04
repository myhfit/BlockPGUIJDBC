package bp.ui.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.swing.Action;

import bp.res.BPResource;
import bp.res.BPResourceDBItem.BPResourceDBTable;
import bp.res.BPResourceJDBCLink;
import bp.ui.actions.BPAction;
import bp.ui.actions.BPPathTreeNodeActions;
import bp.ui.actions.BPProjectsTreeNodeActionsJDBC;
import bp.ui.actions.BPProjectsTreeNodeActionsTable;
import bp.ui.scomp.BPTree;

public class BPProjectsTreeNodeActionFactoryJDBC implements BPProjectsTreeNodeActionFactory
{
	protected BPPathTreeNodeActions m_actptree = new BPPathTreeNodeActions();
	protected BPProjectsTreeNodeActionsJDBC m_actptreeext = new BPProjectsTreeNodeActionsJDBC();
	protected BPProjectsTreeNodeActionsTable m_acttable = new BPProjectsTreeNodeActionsTable();

	public List<Action> getActions(BPTreeComponent<BPTree> tree, BPResource res, int channelid)
	{
		List<Action> rc = null;
		if (res instanceof BPResourceJDBCLink)
		{
			rc = new ArrayList<Action>();
			rc.add(m_actptreeext.getNewAction(tree, (BPResourceJDBCLink) res, channelid));
			rc.add(BPAction.separator());
			rc.add(m_actptree.getDeleteResAction(tree, res, channelid));
			rc.add(BPAction.separator());
			rc.add(m_actptree.getPropertyAction(tree, res, channelid));
		}
		else if (res instanceof BPResourceDBTable)
		{
			rc = new ArrayList<Action>();
			rc.add(m_acttable.getQueryAction(tree, (BPResourceDBTable) res, channelid));
			rc.add(m_acttable.getDDLAction(tree, (BPResourceDBTable) res, channelid));
		}
		return rc;
	}

	public void register(BiConsumer<String, BPProjectsTreeNodeActionFactory> regfunc)
	{
		regfunc.accept(BPResourceJDBCLink.class.getName(), this);
		regfunc.accept(BPResourceDBTable.class.getName(), this);
	}
}
