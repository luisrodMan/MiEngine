package com.nxtr.spengine.dialogs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.ngeneration.furthergui.FOptionPane;
import com.ngeneration.miengine.scene.GameObject;
import com.ngeneration.miengine.util.EngineSerializer;
import com.nxtr.easymng.util.Util;
import com.nxtr.easymng.workspace.Resource;
import com.nxtr.easymng.workspace.SimpleProject;
import com.nxtr.spengine.EngineUtil;

public class MiEngineDialogs {

	public static final Resource newScene(Resource parent, GameObject scene, String name) {
		String suffix = parent.getProject().getConfiguration().getPropertyParser("misc").getString("fileCopyPattern",
				" (#)");
		String tryName = "";
		String ext = "." + EngineUtil.SCENE_EXT;

		boolean nullName = name == null || name.isBlank();

		name = name != null ? name.trim() : "";
		if (name.endsWith(ext))
			name = name.substring(0, name.length() - ext.length());
		while (true && name != null) {
			var valid = name.matches("[^<>:\"/\\|?*]+") && !name.isBlank();

			if (valid) {
				// try names
				tryName = name;
				while (parent.getItemByName(tryName + ext) != null)
					tryName = Util.nextName(suffix, tryName);
			}

			if (valid && tryName.equals(name))
				break;
			else {
				name = FOptionPane.showInputDialog(name.isBlank() ? "New Scene" : "Name Conflict",
						name.isBlank() ? null : "Enter a new name for '" + name + "':", name.isBlank() ? "" : tryName);
			}

		}

		if (name == null) {
			System.out.println("canceled...");
			return null;
		}

		if (nullName)
			scene.setName(name);

		File file = new File(parent.getFile(), name + ext);
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(EngineSerializer.serialize(scene));
			return ((SimpleProject) parent.getProject()).addResource(parent, file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
