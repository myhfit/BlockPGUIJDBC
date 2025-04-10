package bp.ui.shortcut;

import java.util.function.BiConsumer;

public class BPShortCutFactoryJDBC implements BPShortCutFactory
{
	public void register(BiConsumer<String, BPShortCutFactory> regfunc)
	{
		regfunc.accept(BPShortCutSQLPanel.SCKEY_SQLPANEL, this);
	}

	public BPShortCut createShortCut(String key)
	{
		BPShortCut rc = null;
		switch (key)
		{
			case BPShortCutSQLPanel.SCKEY_SQLPANEL:
			{
				rc = new BPShortCutSQLPanel();
				break;
			}
		}
		return rc;
	}
}
