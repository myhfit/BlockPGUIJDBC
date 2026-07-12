package bp.ui.actions;

public enum BPActionConstJDBC implements BPActionConst
{
	TXT_STOPONPAGE,
	ACT_BTNCONNECT,
	ACT_BTNDISCONNECT,
	ACT_BTNRUNPARAMED,
	ACT_BTNRESUME,
	ACT_BTNRESUMETOEND,
	ACT_BTNCOMMIT,
	ACT_BTNROLLBACK,
	;

	@Override
	public String getPackName()
	{
		return "ac_jdbc";
	}
}
