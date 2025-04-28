package com.nxtr.spengine.views.console;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FScrollPane;
import com.ngeneration.furthergui.FTextPane;
import com.ngeneration.furthergui.FurtherApp;
import com.ngeneration.furthergui.layout.BorderLayout;
import com.nxtr.easymng.ViewAdapter;

public class ConsoleView extends ViewAdapter {

	public static String ID = "outputview";
	private FTextPane text = new FTextPane();
	private FPanel container = new FPanel(new BorderLayout());

	public ConsoleView() {
		super(ID, "Output");
		container.add(new FScrollPane(text));
	}

	@Override
	public FComponent getComponent() {
		return container;
	}

	public void appendLine(String line) {
		FurtherApp.getInstance().invokeLater(() -> {
			text.insertString(text.getLength(), line + System.lineSeparator(), null);
		});
	}

	public void clear() {
		FurtherApp.getInstance().invokeLater(() -> {
			text.setText("");
		});
	}

}
