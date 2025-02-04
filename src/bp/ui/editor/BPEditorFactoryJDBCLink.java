package bp.ui.editor;

import bp.config.BPConfig;
import bp.data.BPTextContainer;
import bp.data.BPTextContainerBase;
import bp.format.BPFormat;
import bp.format.BPFormatJDBCLink;
import bp.res.BPResource;
import bp.res.BPResourceFileSystem;
import bp.res.BPResourceJDBCLink;

public class BPEditorFactoryJDBCLink implements BPEditorFactory
{
	public String[] getFormats()
	{
		return new String[] { BPFormatJDBCLink.FORMAT_JDBCLINK };
	}

	public BPEditor<?> createEditor(BPFormat format, BPResource res, BPConfig options, Object... params)
	{
		if (res instanceof BPResourceJDBCLink)
		{
			BPSQLPanel rc = new BPSQLPanel();
			rc.setContextByJDBCLink((BPResourceJDBCLink) res);
			return rc;
		}
		else
			return new BPCodePanel();
	}

	public void initEditor(BPEditor<?> editor, BPFormat format, BPResource res, BPConfig options)
	{
		if (res instanceof BPResourceJDBCLink)
		{

		}
		else
		{
			if (res.isFileSystem() && ((BPResourceFileSystem) res).isFile())
			{
				BPTextContainer con = new BPTextContainerBase();
				con.bind(res);
				((BPCodePanel) editor).bind(con);
			}
		}
	}

	public String getName()
	{
		return "JDBC Link Editor";
	}
}