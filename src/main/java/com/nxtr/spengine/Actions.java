package com.nxtr.spengine;

import com.nxtr.easymng.Application;
import com.nxtr.easymng.Editor;
import com.nxtr.easymng.FireActionEvent;

public class Actions {

	public void saveEditorAction(FireActionEvent event) {
		var viewMng = event.getApplication().getViewManager();
		Editor editor = null;
		if (viewMng.getFocusedView() instanceof Editor edit)
			editor = edit;
		// wrong only save and only if just one is available
		else if (viewMng.getActiveViews().stream().filter(Editor.class::isInstance).findAny()
				.orElse(null) instanceof Editor edit)
			editor = edit;
		editor.doSave();
	}

	public void closeEditorAction(FireActionEvent event) {
		var viewMng = event.getApplication().getViewManager();
		if (viewMng.getFocusedView() instanceof Editor edit) {
			// ? if edit.getUndoManager().hasEdits() ask ??
			Application.getInstance().getViewManager().closeView(edit.getId());
		}
	}

}
