package com.nxtr.spengine.dialogs;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.ngeneration.furthergui.FButton;
import com.ngeneration.furthergui.FComboBox;
import com.ngeneration.furthergui.FDialog;
import com.ngeneration.furthergui.FLabel;
import com.ngeneration.furthergui.FOptionPane;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FTextField;
import com.ngeneration.furthergui.event.DocumentEvent;
import com.ngeneration.furthergui.event.DocumentListener;
import com.ngeneration.furthergui.layout.FlowLayout;
import com.ngeneration.furthergui.layout.GridBagConstraints;
import com.ngeneration.furthergui.layout.GridBagLayout;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.miengine.scene.Component;
import com.ngeneration.miengine.util.Util;
import com.nxtr.spengine.MiEngine;

public class NewScriptDialog extends FDialog {

	FComboBox<String> packagesCombo = new FComboBox<>();
	int[] pressedBtn = new int[] { FOptionPane.NO };
	FTextField field = new FTextField(50);

	public NewScriptDialog(List<String> packages) {
		super("New Script");

		var container1 = getContainerComponent();
		container1.setPadding(new Padding(25, 10, 25, 15));
		container1.setLayout(new FlowLayout(FlowLayout.TOP_TO_BOTTOM, 10, true));

		GridBagLayout layout = new GridBagLayout(2, 2);
		FPanel container = new FPanel();
		container.setLayout(layout);

		FLabel label1 = new FLabel("Package");
		FLabel label2 = new FLabel("Name");
		packages.forEach(p -> packagesCombo.addItem(p));
		packagesCombo.setEditable(true);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.LEFT;
		constraints.margin = new Padding(0, 0, 25, 0);
		constraints.fillVertical = true;
		container.add(label1, constraints);
		constraints = new GridBagConstraints();
		constraints.col = 1;
		constraints.fillHorizontal = true;
		constraints.weightH = 1;
		constraints.margin = new Padding(0, 10, 0, 0);
		container.add(packagesCombo, constraints);
		constraints = new GridBagConstraints();
		constraints.row = 1;
		constraints.col = 0;
		constraints.anchor = GridBagConstraints.LEFT;
		container.add(label2, constraints);
		constraints = new GridBagConstraints();
		constraints.row = 1;
		constraints.col = 1;
		constraints.fillHorizontal = true;
		constraints.weightH = 1;
		container.add(field, constraints);

		container1.add(container);

		FPanel buttonsPanel = new FPanel(new FlowLayout(FlowLayout.RIGHT));
		FButton aceptBtn = new FButton("Accept");
		FButton cancelBtn = new FButton("Cancel");
		buttonsPanel.add(cancelBtn);
		buttonsPanel.add(aceptBtn);
		buttonsPanel.add(new FLabel(" ".repeat(50)));
		container1.add(buttonsPanel);

		pack();

		aceptBtn.setEnabled(false);
		field.addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent event) {
				aceptBtn.setEnabled(field.getLength() > 0);
			}

			@Override
			public void insertUpdate(DocumentEvent event) {
				aceptBtn.setEnabled(field.getLength() > 0);
			}

			@Override
			public void chanedUpdate(DocumentEvent event) {
				aceptBtn.setEnabled(field.getLength() > 0);
			}
		});

		aceptBtn.addActionListener(e -> {
			pressedBtn[0] = FOptionPane.YES;
			dispose();
		});
		cancelBtn.addActionListener(e -> {
			dispose();
		});

	}

	public int setVisible() {
		super.setVisible(true);
		if (pressedBtn[0] == FOptionPane.YES) {
			var fullname = getCannonicalName();
			// create package
			var prj = MiEngine.getProject();
			var source = prj.getSourcesDirectory();
			fullname = fullname.replace("game.", "");
			var dir = prj.getSourcesDirectory();
			File dirFile = new File(dir, getPackage().replace(".", "/"));
			dirFile.mkdirs();

			System.out.println("mkdir: " + dirFile.getAbsolutePath());
			File newFile = new File(dirFile, getName() + ".java");

			var sourceData = "package " + getPackage() + ";\n\n" + "import " + Component.class.getCanonicalName()
					+ ";\n\n" + "public class " + getName() + " extends Component {\n\n" + "\tpublic void start() {\n"
					+ "\t\t\n" + "\t}\n\n" + "\tpublic void update(float delta) {\n" + "\t\t\n" + "\t}\n\n"
					+ "}\n".replace("\n", System.lineSeparator());
			try {
				Util.write(newFile, sourceData);
			} catch (IOException e) {
				e.printStackTrace();
			}
			MiEngine.getInstance().compileScript(getCannonicalName());
			// assign script

			// open Script
			MiEngine.getInstance().openScript(getCannonicalName());
		}

		return pressedBtn[0];
	}

	public void setSelection(String string) {
		if (packagesCombo.containsItem(string))
			packagesCombo.setSelectedItem(string);
		else
			packagesCombo.setValue(string);
	}

	public String getPackage() {
		return (String) packagesCombo.getValue();
	}

	public String getName() {
		return field.getText();
	}

	public String getCannonicalName() {
		String packagePath = getPackage();
		String name = getName();
		return packagePath + "." + name;
	}

}
