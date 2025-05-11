package bp.ui.form;

import java.awt.Component;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import bp.ui.dialog.BPDialogSelectData;
import bp.ui.scomp.BPTextField;
import bp.ui.scomp.BPTextFieldPane;
import bp.util.ClassUtil;

public class BPFormPanelJDBCLink extends BPFormPanelResourceBase
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2916956147431908317L;

	protected BPTextField m_txtdriver;
	protected BPTextField m_txturl;
	protected BPTextField m_txtuser;
	protected BPTextField m_txtpass;
	protected BPTextField m_txtdbcats;

	protected BPTextFieldPane m_pandriver;

	public Map<String, Object> getFormData()
	{
		Map<String, Object> rc = super.getFormData();
		rc.put("driver", m_txtdriver.getNotEmptyText());
		rc.put("url", m_txturl.getNotEmptyText());
		rc.put("user", m_txtuser.getNotEmptyText());
		rc.put("password", m_txtpass.getNotEmptyText());
		rc.put("dbcats", m_txtdbcats.getNotEmptyText());
		return rc;
	}

	protected void initForm()
	{
		super.initForm();
		m_pandriver = makeSingleLineTextFieldPanel(this::onShowDrivers);
		m_txtdriver = m_pandriver.getTextComponent();
		m_txturl = makeSingleLineTextField();
		m_txtuser = makeSingleLineTextField();
		m_txtpass = makeSingleLineTextField();
		m_txtdbcats = makeSingleLineTextField();
		addLine(new String[] { "Driver" }, new Component[] { m_pandriver }, () -> !m_txtdriver.isEmpty());
		addLine(new String[] { "URL" }, new Component[] { m_txturl }, () -> !m_txturl.isEmpty());
		addLine(new String[] { "Username" }, new Component[] { m_txtuser });
		addLine(new String[] { "Password" }, new Component[] { m_txtpass });
		addLine(new String[] { "DB Categories" }, new Component[] { m_txtdbcats });
	}

	protected String onShowDrivers(String old)
	{
		ServiceLoader<Driver> ds = ClassUtil.getExtensionServices(Driver.class);
		List<Driver> dslist = new ArrayList<Driver>();
		for (Driver d : ds)
		{
			dslist.add(d);
		}
		ds = null;
		BPDialogSelectData<Driver> dlg = new BPDialogSelectData<Driver>();
		dlg.setTransFunc(this::driverToText);
		dlg.setSource(dslist);
		dlg.setVisible(true);
		Driver d = dlg.getSelectData();
		if (d != null)
		{
			return d.getClass().getName();
		}
		return "";
	}

	protected String driverToText(Driver d)
	{
		return d.getClass().getName();
	}

	public void showData(Map<String, ?> data, boolean editable)
	{
		super.showData(data, editable);
		setComponentValue(m_txtname, data, "name", editable);
		setComponentValue(m_txtdriver, data, "driver", editable);
		setComponentValue(m_txturl, data, "url", editable);
		setComponentValue(m_txtuser, data, "user", editable);
		setComponentValue(m_txtpass, data, "password", editable);
		setComponentValue(m_txtdbcats, data, "dbcats", editable);
	}
}
