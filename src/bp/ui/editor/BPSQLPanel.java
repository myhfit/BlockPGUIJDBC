package bp.ui.editor;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.Action;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

import bp.BPGUICore;
import bp.config.BPSetting;
import bp.config.UIConfigs;
import bp.data.BPXData;
import bp.data.BPXYDData;
import bp.data.BPXYData;
import bp.format.BPFormatSQL;
import bp.format.BPFormatText;
import bp.format.BPFormatXYData;
import bp.jdbc.BPJDBCContext;
import bp.jdbc.BPJDBCContextBase;
import bp.processor.BPDataProcessor;
import bp.processor.BPDataProcessorManager;
import bp.processor.BPResourceProcessor;
import bp.res.BPResource;
import bp.res.BPResourceByteArray;
import bp.res.BPResourceFile;
import bp.res.BPResourceHolder;
import bp.res.BPResourceJDBCLink;
import bp.ui.actions.BPAction;
import bp.ui.actions.BPSQLActions;
import bp.ui.container.BPEditors.BPEventUIEditors;
import bp.ui.dialog.BPDialogCommon;
import bp.ui.dialog.BPDialogSetting;
import bp.ui.scomp.BPSQLPane;
import bp.ui.scomp.BPSQLResultPane;
import bp.ui.scomp.BPSplitPane;
import bp.ui.scomp.BPTextPane;
import bp.ui.util.CommonUIOperations;
import bp.ui.util.UIStd;
import bp.ui.util.UIUtil;
import bp.util.JSONUtil;
import bp.util.ObjUtil;
import bp.util.TextUtil;

public class BPSQLPanel extends BPCodePanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -89989874622507456L;

	protected BPSplitPane m_sp;
	protected BPSQLResultPane m_result;
	protected BPSQLActions m_actsql;
	protected BPJDBCContext m_context;
	protected String m_status;

	protected BiConsumer<List<BPXData>, Integer> m_adddatafunc;
	protected Consumer<BPXYDData> m_setupqueryfunc;
	protected Consumer<BPResourceJDBCLink> m_changeddsfunc;

	public BPSQLPanel()
	{
		m_adddatafunc = this::onAddData;
		m_setupqueryfunc = this::onQuerySetup;
	}

	protected BPTextPane createTextPane()
	{
		BPTextPane rc = new BPSQLPane();
		UIUtil.createLinePanel(rc, m_scroll);
		return rc;
	}

	protected void init()
	{
		m_changeddsfunc = this::onChangeDS;

		m_actsql = new BPSQLActions(this);
		m_sp = new BPSplitPane(JSplitPane.VERTICAL_SPLIT);
		m_sp.setDividerSize(4);
		m_sp.setBorder(new EmptyBorder(0, 0, 0, 0));
		m_sp.setDividerBorderColor(UIConfigs.COLOR_TEXTHALF(), false);
		m_result = new BPSQLResultPane();
		m_result.setActions(m_actsql.getActions());
		m_result.setDSChangeCallback(m_changeddsfunc);
		m_scroll = new JScrollPane();
		m_txt = createTextPane();
		m_txt.setOnPosChanged(this::onPosChanged);
		m_scroll.setBorder(new MatteBorder(0, 0, 1, 0, UIConfigs.COLOR_WEAKBORDER()));
		m_scroll.getViewport().add(m_txt);
		setLayout(new BorderLayout());
		m_sp.setLeftComponent(m_scroll);
		m_sp.setRightComponent(m_result);
		m_sp.setReservedSize((int) (200 * UIConfigs.UI_SCALE()) - 1);
		add(m_sp, BorderLayout.CENTER);

		m_sp.togglePanel(false);
		initActions();
		initListeners();
	}

	protected void initActions()
	{
		super.initActions();
		List<String> dbcats = new ArrayList<String>();
		if (m_context != null)
		{
			BPResourceJDBCLink jdbclink = m_context.getJDBCLink();
			String dbcstr = jdbclink.getDBCats();
			if (dbcstr != null)
			{
				String[] dbcarr = dbcstr.split(",");
				for (String dbc : dbcarr)
				{
					dbc = dbc.trim();
					if (dbc.length() > 0)
						dbcats.add(dbc);
				}
			}
		}
		Action[] actarr = m_acts;
		List<Action> acts = ObjUtil.makeList((Object[]) actarr);
		acts.add(BPAction.separator());
		{
			Action actsp = BPAction.build("SQL Processor").getAction();
			List<Action> actsub = new ArrayList<Action>();
			List<BPDataProcessor<?, ?>> ps = BPDataProcessorManager.getDataProcessors(BPFormatSQL.FORMAT_SQL);
			for (String dbc : dbcats)
				ps.addAll(BPDataProcessorManager.getDataProcessors(BPFormatSQL.FORMAT_SQL + ".dbcat=" + dbc));
			for (BPDataProcessor<?, ?> p : ps)
			{
				if (p instanceof BPResourceProcessor)
				{
					String pname = p.getName();
					Action actp = BPAction.build(p.getUILabel()).callback((e) -> callSQLResourceProcessor(pname)).getAction();
					actsub.add(actp);
				}
			}
			actsp.putValue(BPAction.SUB_ACTIONS, actsub.toArray(new Action[actsub.size()]));
			acts.add(actsp);
		}
		m_acts = acts.toArray(new Action[0]);
	}

	protected void callSQLResourceProcessor(String pname)
	{
		String str;
		// boolean issel;
		if (m_txt.getSelectionEnd() == 0)
		{
			str = m_txt.getText();
			// issel = false;
		}
		else
		{
			str = m_txt.getSelectedText();
			// issel = true;
		}
		BPResourceHolder src = new BPResourceByteArray(TextUtil.fromString(str, "utf-8"), null, BPFormatText.FORMAT_TEXT, null, null, true);
		str = null;
		BPResourceHolder out = new BPResourceHolder.BPResourceHolderW(null, null, BPFormatXYData.FORMAT_XYDATA, null, null, true);
		BPResourceProcessor<BPResource, BPResource> p = BPDataProcessorManager.getDataProcessorV(pname);
		BPSetting setting = p.getSetting(null);
		boolean outxy = p.canOutput(BPFormatXYData.FORMAT_XYDATA);
		boolean outtext = p.canOutput(BPFormatText.FORMAT_TEXT);
		if (setting != null)
		{
			if (outxy)
			{
				setting.set("OUTPUT", out);
			}
			if (p.needSettingUI())
			{
				BPDialogSetting dlg = new BPDialogSetting();
				dlg.setSetting(setting);
				dlg.setVisible(true);
				if (dlg.getActionResult() != BPDialogCommon.COMMAND_OK)
					return;
				setting = dlg.getResult();
			}
			setting.set("jdbclink", m_context.getJDBCLink());
		}
		if (outxy)
		{
			out = (BPResourceHolder) p.process(src, setting);
			if (out != null)
				CommonUIOperations.openResourceNewWindow(out, new BPFormatXYData(), null, null, null);
		}
		else if (outtext)
		{
			out = (BPResourceHolder) p.process(src, setting);
			String newtxt = out.getData();
			UIStd.info(newtxt);
		}
		else
			p.process(src, setting);
	}

	protected void onChangeDS(BPResourceJDBCLink jdbclink)
	{
		BPJDBCContext context = m_context;
		if (context != null)
		{
			context.close();
			m_context = null;
		}
		setContextByJDBCLink(jdbclink);
	}

	public void setContextByJDBCLink(BPResourceJDBCLink link)
	{
		m_result.setLinkName(link == null ? "" : link.getName());
		m_context = link == null ? null : new BPJDBCContextBase(link);
		m_context.open();

		initActions();
	}

	public void connect()
	{
		checkContext();
		if (m_context == null)
			return;
		m_context.connect().whenComplete(this::onConnected);
		setStatusInfo("Connecting ...");
	}

	protected void onConnected(Boolean result, Throwable t)
	{
		UIUtil.laterUI(() -> setStatusInfo(("Connect " + ((result != null && ((boolean) result == true) ? "Success" : "Failed")))));
		if (t != null)
		{
			m_result.setError(t);
		}
	}

	public void disconnect()
	{
		checkContext();
		if (m_context == null)
			return;
		m_context.disconnect().thenAccept(this::onDisconnected);
		setStatusInfo("Disconnecting ...");
	}

	protected void onDisconnected(Boolean result)
	{
		UIUtil.laterUI(() -> setStatusInfo(("Disconnect " + ((result != null && ((boolean) result == true) ? "Success" : "Failed")))));
	}

	public void showClone(ActionEvent e)
	{
		m_result.showClone(e);
	}

	protected void checkContext()
	{
		if (m_context != null)
			return;

		BPResource res = CommonUIOperations.selectCachedResource((Window) getTopLevelAncestor(), null, ".bpjdbc", true);
		if (res != null)
		{
			BPResourceJDBCLink jdbclink = BPResourceJDBCLink.readLink((BPResourceFile) res);
			onChangeDS(jdbclink);
		}
	}

	public void runWithParams()
	{
		String pstr = UIStd.input("", "Parameters(JSON)", BPGUICore.S_BP_TITLE);
		if (pstr == null)
			return;
		List<Object> plist = JSONUtil.decode(pstr);
		if (plist == null)
		{
			UIStd.info("Parse JSON failed");
			return;
		}
		checkContext();
		if (m_context == null)
			return;
		String sql = m_txt.getSelectedText();
		if (sql == null || sql.length() == 0)
		{
			sql = m_txt.getText();
		}
		if (sql != null)
		{
			sql = sql.trim();
			if (sql.length() > 0)
			{
				SQLCMDTYPE ct = SQLCMDTYPE.find(sql);
				if (ct == null)
				{
					List<String> rct = new ArrayList<String>();
					rct.add("Query");
					rct.add("Execute");
					String s = UIStd.select(rct, "Select Command Type", null);
					if ("Execute".equals(s))
					{
						ct = SQLCMDTYPE.EXECUTE;
					}
					else if ("Query".equals(s))
					{
						ct = SQLCMDTYPE.QUERY;
					}
				}
				if (ct != null)
				{
					switch (ct)
					{
						case QUERY:
						{
							m_context.startQuery(sql, plist.toArray(new Object[plist.size()]), m_setupqueryfunc).whenComplete(this::onQueried);
							setStatusInfo("Query Started ...");
							break;
						}
						case EXECUTE:
						case CONTROL:
						case DEFINITION:
						{
							m_context.execute(sql, plist.toArray(new Object[plist.size()])).whenComplete(this::onExecuted);
							setStatusInfo("Execute Started ...");
							break;
						}
						case COMMIT:
						{
							commit();
							break;
						}
						case ROLLBACK:
						{
							rollback();
							break;
						}
						default:
						{

						}
					}
				}
			}
		}
	}

	public void run()
	{
		checkContext();
		if (m_context == null)
			return;
		String sql = m_txt.getSelectedText();
		if (sql == null || sql.length() == 0)
		{
			sql = m_txt.getText();
		}
		if (sql != null)
		{
			sql = sql.trim();
			if (sql.length() > 0)
			{
				SQLCMDTYPE ct = SQLCMDTYPE.find(sql);
				if (ct == null)
				{
					List<String> rct = new ArrayList<String>();
					rct.add("Query");
					rct.add("Execute");
					String s = UIStd.select(rct, "Select Command Type", null);
					if ("Execute".equals(s))
					{
						ct = SQLCMDTYPE.EXECUTE;
					}
					else if ("Query".equals(s))
					{
						ct = SQLCMDTYPE.QUERY;
					}
				}
				if (ct != null)
				{
					switch (ct)
					{
						case QUERY:
						{
							m_context.startQuery(sql, new Object[] {}, m_setupqueryfunc).whenComplete(this::onQueried);
							setStatusInfo("Query Started ...");
							break;
						}
						case EXECUTE:
						case CONTROL:
						case DEFINITION:
						{
							m_context.execute(sql, new Object[] {}).whenComplete(this::onExecuted);
							setStatusInfo("Execute Started ...");
							break;
						}
						case COMMIT:
						{
							commit();
							break;
						}
						case ROLLBACK:
						{
							rollback();
							break;
						}
						default:
						{

						}
					}
				}
			}
		}
	}

	public void commit()
	{
		m_context.commit().whenComplete(this::onCommited);
		setStatusInfo("Commit Started ...");
	}

	public void rollback()
	{
		m_context.rollback().whenComplete(this::onRollbacked);
		setStatusInfo("Rollback Started ...");
	}

	protected void onQuerySetup(BPXYDData xydata)
	{
		xydata.setDataListener(new WeakReference<BiConsumer<List<BPXData>, Integer>>(m_adddatafunc), null, null);
		final BPXYData datas = new BPXYData.BPXYDataList(xydata, true);
		UIUtil.laterUI(() ->
		{
			setStatusInfo("Fetching ...");
			m_result.setXYData(datas);
		});
	}

	protected void onAddData(List<BPXData> datas, Integer pos)
	{
		UIUtil.laterUI(() ->
		{
			if (pos == null)
			{
				m_result.addXDatas(datas);
			}
		});
	}

	protected void onExecuted(Integer count, Throwable e)
	{
		if (e == null)
		{
			if (count != null)
			{
				m_result.setInfo("Executed " + count + " rows");
				setStatusInfo("Executed done ...");
			}
		}
		else
		{
			m_result.setError(e);
			setStatusInfo("");
		}
	}

	protected void setStatusInfo(String info)
	{
		m_status = info;
		BPEventUIEditors event = new BPEventUIEditors(BPEventUIEditors.EDITOR_STATUS_CHANGED, m_txt.getID(), this, m_status);
		BPGUICore.EVENTS_UI.trigger(m_channelid, event);
	}

	protected void onCommited(Boolean result, Throwable e)
	{
		m_result.setInfo("Commited");
		setStatusInfo("");
	}

	protected void onRollbacked(Boolean result, Throwable e)
	{
		m_result.setInfo("Rollbacked");
		setStatusInfo("");
	}

	protected void onQueried(BPXYDData xydata, Throwable e)
	{
		if (e == null)
		{
			try
			{
				if (xydata != null)
				{
					setStatusInfo("Stop On Page ...");
					xydata.close();
				}
			}
			catch (IOException e2)
			{
			}
		}
		else
		{
			m_result.setError(e);
			setStatusInfo("");
		}
	}

	public String getEditorInfo()
	{
		return m_status;
	}

	public void stop()
	{
		m_context.stopRunSQL();
	}

	public void resume()
	{
		m_context.resumeQuery(this::onResumeSetup).whenComplete(this::onQueried);
	}

	public void resumeToEnd()
	{
		m_context.resumeQueryToEnd(this::onResumeSetup).whenComplete(this::onEndQueried);
	}

	protected void onResumeSetup(BPXYDData xydata)
	{
		xydata.setDataListener(new WeakReference<BiConsumer<List<BPXData>, Integer>>(m_adddatafunc), null, null);
		UIUtil.laterUI(() ->
		{
			setStatusInfo("Fetching ...");
		});
	}

	public void clearResource()
	{
		if (m_context != null)
		{
			final BPJDBCContext context = m_context;
			m_context = null;
			context.disconnect();
			context.close();
		}
		m_result.clearResource();
		setStatusInfo("");
		super.clearResource();
	}

	protected void onEndQueried(BPXYDData xydata, Throwable e)
	{
		if (e == null)
		{
			try
			{
				if (xydata != null)
				{
					setStatusInfo("Query Ended");
					xydata.close();
				}
			}
			catch (IOException e2)
			{
			}
		}
		else
		{
			m_result.setError(e);
			setStatusInfo("");
		}
	}

	public enum SQLCMDTYPE
	{
		QUERY(1, new String[] { "SELECT", "PRAGMA" }), EXECUTE(1, new String[] { "INSERT", "UPDATE", "DELETE" }), COMMIT(2, new String[] { "COMMIT" }), ROLLBACK(2, new String[] { "ROLLBACK" }), SAVEPOINT(1, new String[] { "SAVEPOINT" }), CONTROL(1,
				new String[] { "GRANT", "REVOKE" }), DEFINITION(1, new String[] { "CREATE", "ALTER", "DROP" }),
		// CCL(1, new String[] {"DECLARE CURSOR","FETCH INTO",""})
		;

		private String[] m_checkstrs;
		private int m_checktype;

		private SQLCMDTYPE()
		{

		}

		private SQLCMDTYPE(int checktype, String[] checkstrs)
		{
			m_checktype = checktype;
			m_checkstrs = checkstrs;
		}

		public boolean match(String sql)
		{
			if (m_checktype == 1)
			{
				for (String checkstr : m_checkstrs)
				{
					if (sql.startsWith(checkstr))
					{
						return true;
					}
				}
				return false;
			}
			else if (m_checktype == 2)
			{
				for (String checkstr : m_checkstrs)
				{
					if (sql.equals(checkstr))
					{
						return true;
					}
				}
				return false;
			}
			return true;
		}

		public static SQLCMDTYPE find(String sql)
		{
			String usql = sql.trim().toUpperCase();
			for (SQLCMDTYPE ct : SQLCMDTYPE.values())
			{
				if (ct.match(usql))
					return ct;
			}
			return null;
		}
	}
}
