package bp.ui.scomp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;

import bp.BPCore;
import bp.cache.BPCacheDataFileSystem;
import bp.cache.BPTreeCacheNode;
import bp.config.UIConfigs;
import bp.data.BPXData;
import bp.data.BPXYDData;
import bp.data.BPXYDDataBase;
import bp.data.BPXYData;
import bp.res.BPResource;
import bp.res.BPResourceFile;
import bp.res.BPResourceFileLocal;
import bp.res.BPResourceJDBCLink;
import bp.typeext.KV.KVs;
import bp.ui.actions.BPAction;
import bp.ui.actions.BPXYDataCloneActions;
import bp.ui.container.BPToolBarSQ;
import bp.ui.scomp.BPTable.BPTableModel;
import bp.ui.scomp.BPTable.BPTableRendererCommonObj;
import bp.ui.table.BPTableFuncsXY;
import bp.ui.util.CommonDataUIProcs;
import bp.ui.util.CommonUIOperations;
import bp.ui.util.UIUtil;
import bp.util.LogicUtil.WeakRefGo;
import bp.util.ObjUtil;
import bp.util.Std;

public class BPSQLResultPane extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4498545149200814565L;

	protected BPToolBarSQ m_toolbar;
	protected BPTable<BPXData> m_table;
	protected JScrollPane m_scroll;
	protected BPLabel m_lbllinkname;
	protected JPanel m_pnlcenter;

	protected BPTableModel<BPXData> m_model;
	protected BPTableFuncsXY m_funcs;
	protected BPCommonDataPanel m_previewpan;

	protected BPTextPane m_txt;

	protected boolean m_istext = false;

	protected WeakRefGo<Consumer<BPResourceJDBCLink>> m_dschangeref = new WeakRefGo<Consumer<BPResourceJDBCLink>>();

	public BPSQLResultPane()
	{
		init();
	}

	protected void init()
	{
		m_toolbar = new BPToolBarSQ();
		m_scroll = new JScrollPane();
		m_txt = new BPTextPane();
		m_table = new BPTable<BPXData>();
		m_lbllinkname = new BPLabel(" JDBC:[Empty] ");
		m_lbllinkname.addMouseListener(new UIUtil.BPMouseListener(null, this::onShowDS, null, null, null));
		m_pnlcenter=new JPanel();
		m_previewpan = new BPCommonDataPanel();
		m_previewpan.setPreferredSize(new Dimension(400,0));
		m_previewpan.setMode(CommonDataUIProcs.MODE_KV);

		initTable(m_table);

		m_lbllinkname.setLabelFont();
		m_lbllinkname.setFloatLabel();
		m_scroll.setBorder(new EmptyBorder(0, 0, 0, 0));
		m_pnlcenter.setBorder(new MatteBorder(1, 0, 0, 0, UIConfigs.COLOR_STRONGBORDER()));
		m_previewpan.setBorder(new MatteBorder(0, 1, 0, 0, UIConfigs.COLOR_WEAKBORDER()));
		m_previewpan.setVisible(false);

		setLayout(new BorderLayout());
		m_pnlcenter.setLayout(new BorderLayout());

		m_scroll.setViewportView(m_table);
		add(m_toolbar, BorderLayout.NORTH);
		add(m_pnlcenter, BorderLayout.CENTER);
		m_pnlcenter.add(m_scroll, BorderLayout.CENTER);
	}

	public void setDSChangeCallback(Consumer<BPResourceJDBCLink> cb)
	{
		m_dschangeref.setTarget(cb);
	}

	protected void onShowDS(MouseEvent e)
	{
		int mnusize = 10;
		List<BPTreeCacheNode<BPCacheDataFileSystem>> cs = BPCore.FS_CACHE.searchFileByName(".bpjdbc", null, mnusize);

		JPopupMenu pop = new JPopupMenu();
		for (BPTreeCacheNode<BPCacheDataFileSystem> node : cs)
		{
			String fname = node.getValue().getFullName();
			BPMenuItem item = new BPMenuItem(BPAction.build(fname).callback(e2 -> changeDS(fname)).getAction());
			pop.add(item);
		}
		if (cs.size() == mnusize)
		{
			BPMenuItem itemm = new BPMenuItem(BPAction.build("...").callback(e2 -> selectDS()).getAction());
			pop.add(itemm);
		}
		pop.show(m_lbllinkname, 0, m_lbllinkname.getHeight() + 1);
	}

	protected void selectDS()
	{
		BPResource res = CommonUIOperations.selectCachedResource((Window) getTopLevelAncestor(), null, ".bpjdbc", true);
		if (res != null)
		{
			BPResourceJDBCLink jdbclink = BPResourceJDBCLink.readLink((BPResourceFile) res);
			changeJDBCLink(jdbclink);
		}
	}

	protected void changeDS(String fname)
	{
		BPResourceFile f = new BPResourceFileLocal(fname);
		BPResourceJDBCLink jdbclink = BPResourceJDBCLink.readLink(f);
		changeJDBCLink(jdbclink);
	}

	protected void changeJDBCLink(BPResourceJDBCLink jdbclink)
	{
		m_dschangeref.exec(func ->
		{
			func.accept(jdbclink);
			return null;
		});
	}

	protected void initTable(BPTable<BPXData> table)
	{
		m_table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		m_table.setMonoFont();
		m_table.setCommonRenderAlign();
		m_table.setDefaultRenderer(Object.class, new BPTableRendererCommonObj());
		m_table.getSelectionModel().addListSelectionListener(this::onSelect);
	}

	public void setActions(Action[] actions, JComponent accparent)
	{
		m_toolbar.setActions(actions, accparent);
		m_toolbar.addGlue();
		m_toolbar.add(m_lbllinkname);
	}

	public void setXYData(BPXYData data)
	{
		if (m_istext)
		{
			m_istext = false;
			m_scroll.setViewportView(m_table);
		}
		m_funcs = new BPTableFuncsXY(data);
		m_model = new BPTableModel<BPXData>(m_funcs);
		m_model.setDatas(data.getDatas());
		m_table.setVisible(true);
		m_table.setModel(m_model);
		m_table.initRowSorter();
		m_table.installRowHeader(m_scroll);
		m_table.setColumnWidthBatch(UIUtil.scale(180), UIUtil.scale(60));
	}

	public void addXDatas(List<BPXData> datas)
	{
		m_model.addAll(datas);
		m_model.fireTableDataChanged();
	}

	public void setInfo(String info)
	{
		if (!m_istext)
		{
			m_istext = true;
			m_scroll.setRowHeader(null);
			m_scroll.setViewportView(m_txt);
		}
		m_txt.setText(info);
		m_table.setVisible(false);
		clearPreview();
	}

	public void setError(Throwable e)
	{
		Throwable tr = e;
		if (tr instanceof RuntimeException)
		{
			tr = tr.getCause();
		}
		if (!m_istext)
		{
			m_istext = true;
			m_scroll.setRowHeader(null);
			m_scroll.setViewportView(m_txt);
			clearPreview();
		}
		String t = tr.getMessage();
		if (Std.getStdMode() >= Std.STDMODE_DEBUG)
			t += ObjUtil.joinArray(e.getStackTrace(), "\n\t", ste -> ste.toString(), false);
		m_txt.setText(t);
		m_table.setVisible(false);
	}

	protected void clearPreview()
	{
		m_previewpan.setData(new KVs(ObjUtil.makeMap()));
		m_previewpan.initByData(true);
	}

	public void setLinkName(String linkname)
	{
		m_lbllinkname.setText(" JDBC:" + linkname + " ");
	}

	protected void setPreviewData()
	{
		if (!m_table.isVisible())
			return;
		Map<String, Object> kv = new LinkedHashMap<String, Object>();
		int si = m_table.getSelectedRow();
		if (si > -1)
		{
			BPXData xdata = m_funcs.getRawData().getDatas().get(si);
			String[] cols = m_funcs.getColumnNames();
			for (int i = 0; i < cols.length; i++)
			{
				String k = cols[i];
				kv.put(k, m_funcs.getValue(xdata, si, i));
			}
			
		}
		KVs kvs = new KVs(kv);
		m_previewpan.setData(kvs);
		m_previewpan.initByData(true);
	}
	
	public void togglePreviewPanel(Boolean v)
	{
		boolean oldp = m_previewpan.isVisible();
		boolean isp = !oldp;
		if (v != null)
			isp = v;
		m_previewpan.setVisible(isp);
		if (isp != oldp)
		{
			if (isp)
			{
				setPreviewData();
				m_pnlcenter.add(m_previewpan, BorderLayout.EAST);
				m_previewpan.requestEditorFocus();
			}
			else
			{
				m_pnlcenter.remove(m_previewpan);
			}
			updateUI();
		}
	}

	public boolean isPreviewVis()
	{
		return m_previewpan.isVisible();
	}

	protected void onSelect(ListSelectionEvent e)
	{
		if (!e.getValueIsAdjusting() && m_previewpan.isVisible())
			setPreviewData();
	}

	public void showClone(ActionEvent e)
	{
		if (m_model == null)
			return;
		List<BPXData> datas = m_model.getDatas();
		BPXYDData xydata = createSaveData(m_funcs.getColumnNames(), m_funcs.getColumnClasses(), m_funcs.getColumnLabels(), datas);
		Action[] acts = BPXYDataCloneActions.getActions(xydata, null);
		if (acts != null && acts.length > 0)
		{
			JPopupMenu pop = new JPopupMenu();
			JComponent[] comps = UIUtil.makeMenuItems(acts);
			for (JComponent comp : comps)
			{
				pop.add(comp);
			}
			JComponent source = (JComponent) e.getSource();
			JComponent par = (JComponent) source.getParent();
			pop.show(par, source.getX(), source.getY() + source.getHeight());
		}
	}

	protected BPXYDData createSaveData(String[] colnames, Class<?>[] colclasses, String[] collabels, List<BPXData> datas)
	{
		BPXYDDataBase xydata = new BPXYDDataBase();
		xydata.setColumnNames(colnames);
		xydata.setColumnClasses(colclasses);
		xydata.setColumnLabels(collabels);
		xydata.setDatas(datas);
		return xydata;
	}

	public void clearResource()
	{
		m_table.clearResource();
		BPTableFuncsXY funcs = m_funcs;
		if (funcs != null)
			funcs.clear();
		m_scroll.setRowHeaderView(null);
		m_scroll.setViewportView(null);
		removeAll();
	}
}
