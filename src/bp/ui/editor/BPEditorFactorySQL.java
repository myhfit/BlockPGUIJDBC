package bp.ui.editor;

import bp.config.BPConfig;
import bp.data.BPTextContainer;
import bp.data.BPTextContainerBase;
import bp.format.BPFormat;
import bp.format.BPFormatSQL;
import bp.res.BPResource;
import bp.res.BPResourceFileSystem;
import bp.res.BPResourceJDBCLink;

public class BPEditorFactorySQL implements BPEditorFactory
{
	public String[] getFormats()
	{
		return new String[] { BPFormatSQL.FORMAT_SQL };
	}

	public BPEditor<?> createEditor(BPFormat format, BPResource res, BPConfig options, Object... params)
	{
		BPSQLPanel rc = new BPSQLPanel();
		if (params != null && params.length > 0)
			rc.setContextByJDBCLink((BPResourceJDBCLink) params[0]);
		return rc;
	}

	public void initEditor(BPEditor<?> editor, BPFormat format, BPResource res, BPConfig options)
	{
		if (res.isFileSystem() && ((BPResourceFileSystem) res).isFile())
		{
			BPTextContainer con = new BPTextContainerBase();
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

	public String getName()
	{
		return "SQL Editor";
	}
}