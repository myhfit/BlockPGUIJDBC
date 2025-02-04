package bp.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;

import javax.swing.Action;

import bp.data.BPXData;
import bp.data.BPXYData;
import bp.jdbc.BPJDBCContextBase;
import bp.project.BPResourceProject;
import bp.project.BPResourceProjectJDBC;
import bp.res.BPResourceDir;
import bp.res.BPResourceJDBCLink;
import bp.ui.dialog.BPDialogBlock;
import bp.ui.dialog.BPDialogSelectResource2;
import bp.ui.dialog.BPDialogSelectResource2.SELECTSCOPE;
import bp.ui.util.UIStd;
import bp.ui.util.UIUtil;

public class BPDataActionFactoryJDBC implements BPDataActionFactory
{
	public Action[] getAction(Object data, String actionname, Runnable loaddatafunc)
	{
		Action[] rc = null;
		if (data != null && actionname != null)
		{
			if (data instanceof BPXYData && ACTIONNAME_CLONEDATA.equals(actionname))
			{
				Action actclonecsv = BPAction.build("JDBC(SQL)").callback(new DataActionProcessor<BPXYData>((BPXYData) data, BPDataActionFactoryJDBC::cloneXYDataToTable, loaddatafunc)).getAction();
				rc = new Action[] { actclonecsv };
			}
		}
		return rc;
	}

	private final static String genSQL(BPXYData xydata)
	{
		return "";
	}

	private final static String mappingSQL(String sql, List<String> colmapping)
	{
		StringBuilder sb = new StringBuilder();
		int l = sql.length();
		for (int i = 0; i < l; i++)
		{
			char c = sql.charAt(i);
			if (c == '$' && l > (i + 2) && sql.charAt(i + 1) == '{')
			{
				int vi = sql.indexOf('}', i + 2);
				if (vi > i + 1)
				{
					String col = sql.substring(i + 2, vi);
					colmapping.add(col);
					sb.append("?");
					i = vi;
				}

			}
			else
			{
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private final static void cloneXYDataToTable(BPXYData xydata, ActionEvent event)
	{
		BPDialogSelectResource2 dlg = new BPDialogSelectResource2();
		dlg.setTargetFilter((res) -> res instanceof BPResourceJDBCLink);
		dlg.setScope(SELECTSCOPE.PROJECT);
		dlg.setFilter((res) ->
		{
			if (res instanceof BPResourceProject)
			{
				return res instanceof BPResourceProjectJDBC;
			}
			else if (res.isProjectResource())
			{
				return res instanceof BPResourceJDBCLink;
			}
			else if (res instanceof BPResourceDir)
			{
				return true;
			}
			return false;
		});
		dlg.showOpen();
		BPResourceJDBCLink link = (BPResourceJDBCLink) dlg.getSelectedResource();
		if (link == null)
			return;
		String sql = genSQL(xydata);
		sql = UIStd.textarea(sql, "Confirm SQL", true);
		if (sql == null)
			return;
		sql = sql.trim();
		if (sql.length() == 0)
			return;
		List<String> colmapping = new ArrayList<String>();
		sql = mappingSQL(sql, colmapping);
		BPJDBCContextBase context = new BPJDBCContextBase(link);
		context.connect();
		String[] cols = xydata.getColumnNames();
		Map<String, Integer> colmap = new HashMap<String, Integer>();
		for (int i = 0; i < cols.length; i++)
		{
			colmap.put(cols[i], i);
		}
		int[] tarcols = new int[colmapping.size()];
		List<BPXData> datas = xydata.getDatas();
		for (int i = 0; i < tarcols.length; i++)
		{
			tarcols[i] = colmap.get(colmapping.get(i));
		}
		Iterator<BPXData> dit = datas.iterator();
		AtomicReference<BPDialogBlock<Boolean>> dlgref = new AtomicReference<BPDialogBlock<Boolean>>(null);
		AtomicInteger counter = new AtomicInteger(-1);
		int l = datas.size();
		BiFunction<Integer, Exception, Object[]> cb = (r, e) ->
		{
			if (e != null)
				return null;
			if (dit.hasNext())
			{
				int c = counter.incrementAndGet();
				if ((c % 1000) == 0)
				{
					BPDialogBlock<Boolean> bd = dlgref.get();
					if (bd != null)
					{
						UIUtil.laterUI(() -> bd.refreshText(c + "/" + l));
					}
				}
				BPXData xdata = dit.next();
				Object[] ps = new Object[tarcols.length];
				for (int i = 0; i < tarcols.length; i++)
				{
					ps[i] = xdata.getColValue(tarcols[i]);
				}
				return ps;
			}
			else
			{
				return null;
			}
		};
		context.connect();
		final String tsql = sql;
		UIUtil.block(() -> context.interactiveBatchExecute(tsql, cb), "Running", true, true, bd -> dlgref.set(bd));
		context.commit();
		context.disconnect();
		context.close();
	}
}
