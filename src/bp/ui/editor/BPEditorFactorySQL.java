package bp.ui.editor;

import bp.config.BPConfig;
import bp.config.BPSetting;
import bp.config.BPSettingBase;
import bp.config.BPSettingItem;
import bp.data.BPTextContainer;
import bp.data.BPTextContainerBase;
import bp.format.BPFormat;
import bp.format.BPFormatJDBCLink;
import bp.format.BPFormatSQL;
import bp.res.BPResource;
import bp.res.BPResourceFile;
import bp.res.BPResourceFileSystem;
import bp.res.BPResourceJDBCLink;
import bp.util.LogicUtil;
import bp.util.TextUtil;

public class BPEditorFactorySQL implements BPEditorFactory
{
	public String[] getFormats()
	{
		return new String[] { BPFormatSQL.FORMAT_SQL, BPFormatJDBCLink.FORMAT_JDBCLINK };
	}

	public BPEditor<?> createEditor(BPFormat format, BPResource res, BPConfig options, Object... params)
	{
		BPSQLPanel rc = new BPSQLPanel();
		if (BPFormatSQL.FORMAT_SQL.equals(format.getName()))
		{
			if (params != null && params.length > 0)
				rc.setContextByJDBCLink((BPResourceJDBCLink) params[0]);
		}
		else if (BPFormatJDBCLink.FORMAT_JDBCLINK.equals(format.getName()))
		{
			if (res != null)
			{
				if (BPResourceJDBCLink.RESTYPE_JDBCLINK.equals(res.getResType()))
					rc.setContextByJDBCLink((BPResourceJDBCLink) res);
				else if (res instanceof BPResourceFile && ((BPResourceFile) res).exists())
				{
					res = BPResourceJDBCLink.readLink((BPResourceFile) res);
					rc.setContextByJDBCLink((BPResourceJDBCLink) res);
				}
			}
		}
		return rc;
	}

	public void initEditor(BPEditor<?> editor, BPFormat format, BPResource res, BPConfig options)
	{
		if (BPFormatSQL.FORMAT_SQL.equals(format.getName()))
		{
			if (res != null && res.isFileSystem() && ((BPResourceFileSystem) res).isFile())
			{
				BPTextContainer con = new BPTextContainerBase();
				if (options != null)
					LogicUtil.VLF(((String) options.get("encoding")), TextUtil::checkNotEmpty, con::setEncoding);
				con.bind(res);
				((BPSQLPanel) editor).bind(con, ((BPResourceFileSystem) res).getTempID() != null);
				if (options != null)
				{
					String dv = options.get("_DEFAULT_VALUE");
					if (dv != null)
						((BPSQLPanel) editor).getTextPanel().setText(dv);
				}
			}
		}
	}

	public BPSetting getSetting(String formatkey)
	{
		BPSettingBase rc = new BPSettingBase();
		rc.addItem(BPSettingItem.create("encoding", "Encoding", BPSettingItem.ITEM_TYPE_TEXT, null));
		return rc;
	}

	public String getName()
	{
		return "SQL Editor";
	}
}