package bp.ui.actions;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.Action;

import bp.BPGUICore;
import bp.jdbc.BPJDBCContextBase;
import bp.jdbc.BPJDBCHelper;
import bp.res.BPResource;
import bp.res.BPResourceDBItem.BPResourceDBColumn;
import bp.res.BPResourceDBItem.BPResourceDBSchema;
import bp.res.BPResourceDBItem.BPResourceDBTable;
import bp.res.BPResourceJDBCLink;
import bp.res.BPResourceJDBCLink.DBColumn;
import bp.res.BPResourceJDBCLink.DBColumnCache;
import bp.res.BPResourceJDBCLink.DBStruct;
import bp.ui.scomp.BPTree;
import bp.ui.tree.BPTreeComponent;
import bp.ui.util.UIStd;
import bp.util.JDBCUtil;
import bp.util.ObjUtil;
import bp.util.Std;

public class BPProjectsTreeNodeActionsTable
{
	public BPProjectsTreeNodeActionsTable()
	{
	}

	public BPAction getQueryAction(BPTreeComponent<BPTree> tree, BPResourceDBTable table, int channelid)
	{
		BPAction rc = BPAction.build("Query").callback(e ->
		{
			BPResourceDBSchema schema = (BPResourceDBSchema) table.getParentResource();
			BPResourceJDBCLink jdbclink = (BPResourceJDBCLink) schema.getParentResource();
			if (jdbclink != null)
			{
				String schemaname = "";
				if (schema.getName() != null && schema.getName().length() > 0)
					schemaname = (schema + ".");
				String tablename = schemaname + table.getName();
				BPGUICore.runOnMainFrame(mf -> mf.createEditorByFileSystem("untitled.sql", "SQL", "SQL Editor", ObjUtil.makeMap("_DEFAULT_VALUE", "SELECT * FROM " + tablename), jdbclink));
			}
		}).mnemonicKey(KeyEvent.VK_Q).getAction();
		return rc;
	}

	public BPAction getDDLAction(BPTreeComponent<BPTree> tree, BPResourceDBTable table, int channelid)
	{
		BPAction rc = BPAction.build("Get DDL").mnemonicKey(KeyEvent.VK_D).getAction();
		BPAction actddlcreate = BPAction.build("Create Table").callback(e ->
		{
			BPResourceDBSchema schema = (BPResourceDBSchema) table.getParentResource();
			BPResourceJDBCLink jdbclink = (BPResourceJDBCLink) schema.getParentResource();
			BPJDBCHelper helper = JDBCUtil.getHelper(jdbclink.getDriver());
			if (helper == null)
			{
				UIStd.info("No helper for:" + jdbclink.getDriver());
				return;
			}
			String tablename = table.getName();
			String schemaname = schema.getName();
			if (helper.checkFeature(BPJDBCHelper.ACT_GETDDL_CREATETABLE))
			{
				BPJDBCContextBase context = new BPJDBCContextBase(jdbclink);
				context.connect();
				try
				{
					String ddl = helper.doAction(BPJDBCHelper.ACT_GETDDL_CREATETABLE, context, tablename, schemaname);
					UIStd.textarea(ddl, "DDL for Table:" + tablename);
				}
				finally
				{
					context.close();
				}
			}
			else
			{
				UIStd.info("Not support this action on:" + helper.getDBName());
			}
		}).mnemonicKey(KeyEvent.VK_S).getAction();
		BPAction actddlinsert = BPAction.build("Insert").callback(e ->
		{
			BPResourceDBSchema schema = (BPResourceDBSchema) table.getParentResource();
			BPResourceJDBCLink jdbclink = (BPResourceJDBCLink) schema.getParentResource();
			String schemaprefix = "";
			if (schema != null && schema.getName() != null)
				schemaprefix = schema.getName();
			if (schemaprefix.length() > 0)
				schemaprefix += ".";
			String tablename = table.getName();
			String rtablename = schemaprefix + tablename;
			StringBuilder sb = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			List<String> colnames = new ArrayList<String>();
			BPResource[] subs = table.listResources();
			if (subs != null)
			{
				for (BPResource res : subs)
				{
					if (res instanceof BPResourceDBColumn)
					{
						colnames.add(res.getName());
					}
				}
			}
			else
			{
				BPJDBCContextBase context = new BPJDBCContextBase(jdbclink);
				DBStruct dbs = new DBStruct();
				dbs.columns = new ConcurrentHashMap<String, DBColumnCache>();
				context.connect();
				try
				{
					context.listColumns(new CopyOnWriteArrayList<>(new String[] { rtablename }), dbs).toCompletableFuture().get();
					Collection<DBColumnCache> ccs = dbs.columns.values();
					for (DBColumnCache cc : ccs)
					{
						for (DBColumn c : cc)
							colnames.add(c.name);
					}
				}
				catch (InterruptedException | ExecutionException e1)
				{
					Std.err(e1);
				}
				finally
				{
					context.close();
				}
			}
			sb.append("INSERT INTO ");
			sb.append(schemaprefix);
			sb.append(tablename);
			sb.append("(");
			{
				boolean flag = false;
				for (String colname : colnames)
				{
					if (flag)
					{
						sb.append(",");
						sb2.append(",");
					}
					else
						flag = true;
					sb.append(colname);
					sb2.append("?");
				}
			}
			sb.append(") VALUES(");
			sb.append(sb2.toString());
			sb.append(")");
			UIStd.textarea(sb.toString(), "DDL for Table:" + tablename);
		}).mnemonicKey(KeyEvent.VK_I).getAction();
		rc.putValue(BPAction.SUB_ACTIONS, new Action[] { actddlinsert, BPAction.separator(), actddlcreate });
		return rc;
	}
}
