package bp.ui.form;

import java.util.function.BiConsumer;

public class BPFormPanelFactoryJDBC implements BPFormPanelFactory
{
	public void register(BiConsumer<String, Class<? extends BPFormPanel>> regfunc)
	{
		regfunc.accept("bp.project.BPResourceProjectJDBC", BPFormPanelJDBCProject.class);
		regfunc.accept("bp.res.BPResourceJDBCLink", BPFormPanelJDBCLink.class);
	}

}
