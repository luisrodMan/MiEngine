package com.nxtr.spengine;

import java.util.LinkedList;

import com.ngeneration.furthergui.FComponent;
import com.ngeneration.furthergui.FFrame;
import com.ngeneration.furthergui.FPanel;
import com.ngeneration.furthergui.FScrollPane;
import com.ngeneration.furthergui.FTabbedPane;
import com.ngeneration.furthergui.FTextField;
import com.ngeneration.furthergui.FurtherApp;
import com.ngeneration.furthergui.drag.DragInterface;
import com.ngeneration.furthergui.drag.DropEvent;
import com.ngeneration.furthergui.drag.Flavor;
import com.ngeneration.furthergui.drag.Transferable;
import com.ngeneration.furthergui.event.FocusEvent;
import com.ngeneration.furthergui.event.FocusListener;
import com.ngeneration.furthergui.graphics.Color;
import com.ngeneration.furthergui.layout.Layout;
import com.ngeneration.furthergui.math.Padding;
import com.ngeneration.furthergui.text.FTextComponent;

public class GUITest {

	private FFrame frame = new FFrame("Testing xd");

	public GUITest() {

		var component = testTextComponents();
		frame.getContainerComponent().add(component);

		frame.setDefaultCloseOperation(FFrame.EXIT_ON_CLOSE);
		frame.setDimension(1100, 850);
//		frame.setPrefferedSize(new Dimension(200, 200));
		frame.getContainerComponent().setBackground(Color.RED);
		frame.setLocationRelativeTo(null);
		frame.validate();
		frame.setVisible(true);
	}

	private FComponent testTextComponents() {
		var container = new FPanel((Layout) null);

		FTextComponent textArea = new FTextComponent();
		FTextComponent textArea2 = new FTextComponent();
		textArea.setText("6.- Iterate Continuing Project Ideas Levels and Fix Bugs On Demand!\r\n"
				+ "7.- Once completed Run On Android!!!\r\n" + "8.- Integrate payments!\r\n" + "9.- Release!!!!\r\n"
				+ "3.- Fix TextField Component\r\n" + "4.- Fix GUI Nasty Bugs\r\n" + "\r\n" + "######### FIXES\r\n"
				+ "\r\n" + "Fix input controls\r\n" + "# textfield\r\n" + "fix input text\r\n"
				+ "clear selection on focus lost\r\n" + "paint background on click ???\r\n"
				+ "fix no enter in textfields xd\r\n" + "\r\n" + "\r\n"
				+ "Start creating temp world and play on finished scale!!!!!!!!!!!!\r\n"
				+ "Drag operations | resource to {Outline|Editor|inspector}   ....   Outline to Resources\r\n"
				+ "Fix drag out of Window????\r\n"
				+ "Fix repaint when window close  -> clear clip  target.clip(every window) clear using background color\r\n"
				+ "Fix Rename resource (\"update reference data index\");\r\n"
				+ "Fix unknown resources crashes inspector  not indexed xddxdx???\r\n" + "delete resources!\r\n"
				+ "Fix add Component btn (add all components and sort by name!!??)\r\n" + "\r\n"
				+ "Fix Tree Resize on open use Icon\r\n" + "Fix Tree   use Keyboard to open close nodes xddxxd!");
		textArea.setBounds(10, 50, 300, 600);
		textArea.setPadding(new Padding(30));

		textArea2.setText(textArea.getText());
		FScrollPane scrolled = new FScrollPane(textArea2);
		scrolled.setBounds(400, 50, 300, 400);

		FTextField field = new FTextField();
		field.setBounds(400, 500, 100, field.getPrefferedSize().height);

		container.add(textArea);
		container.add(scrolled);
		container.add(field);
		return container;
	}

	private FComponent testDragAndDrop() {

		var container = new FPanel();
		container.setLayout(null);

		var panel1 = new DraggablePanel();
		panel1.setBackground(Color.RED);
		panel1.setName("panel 1");
		var panel2 = new DraggablePanel();
		panel2.setBackground(Color.BLUE);
		panel2.setName("panel 2");

		var panel3 = new DraggablePanel();
		panel3.setBackground(Color.GREEN);
		panel3.setName("panel 3");

		int size = 100;
		panel1.setBounds(size, size, size, size);
		panel2.setBounds(size * 3, size, size, size);
		panel3.setBounds(size * 1, size * 3, size, size);

		container.add(panel1);
		container.add(panel2);
		container.add(panel3);

		return container;
	}

	private class DraggablePanel extends FPanel implements DragInterface {

		@Override
		public void onDrag(DropEvent event) {
			event.acept(new Transferable() {
				@Override
				public boolean isFlavorSupported(Class<? extends Flavor> flavor) {
					return Flavor.ObjectFlavor.class.isAssignableFrom(flavor);
				}

				@Override
				public <T extends Flavor> T getFlavor(Class<T> type) {
					return type.cast(new Flavor.ObjectFlavor(getBackground()));
				}
			}, DropEvent.COPY_MODE);
		}

		@Override
		public void onDrop(DropEvent event) {
			if (event.getTransferable().isFlavorSupported(Flavor.ObjectFlavor.class))
				if (event.getEventType() == DropEvent.ACCEPTED_EVENT)
					onDropAccepted(event);
				else
					event.acept(DropEvent.COPY_MODE);
		}

		public void onDropAccepted(DropEvent event) {
			System.out.println("dropped in " + this);
			var flavor = event.getTransferable().getFlavor(Flavor.ObjectFlavor.class);
			setBackground((Color) flavor.getValue());
			repaint();
		}

	}

	private FComponent testTabs() {
		FTabbedPane tabs = new FTabbedPane();

		FPanel panel1 = new FPanel();
		panel1.setName("panel 1");
		panel1.setBackground(Color.RED);
		panel1.setFocusable(true);

		FPanel panel2 = new FPanel();
		panel2.setName("panel 2");
		panel2.setBackground(Color.BLUE);
		panel2.setFocusable(true);

		tabs.addTab("tab1", panel1);
		tabs.addTab("tab2", panel2);

		FocusListener focus = new FocusListener() {
			@Override
			public void focusLost(FocusEvent event) {
				System.out.println("event unfocused: " + event.getSource());
			}

			@Override
			public void focusGained(FocusEvent event) {
				System.out.println("event focused: " + event.getSource());
			}
		};
		tabs.addChangeListener(e -> {
			System.out.println("tabs changed: " + tabs.getSelectedIndex());
		});
		panel1.addFocusListener(focus);
		panel2.addFocusListener(focus);
		return tabs;
	}

	public static void main(String[] args) {
		//
//		testGUI();
		var list = new LinkedList<String>();
		list.add("hola135434534");
		list.add("hola2");

		var lista2 = list.stream().filter(v -> {
			System.out.println("listao size: " + list.size());
			return v.length() > 10;
		}).toList();

		System.out.println("lista 1");
		list.forEach(System.out::println);
		System.out.println("lista 2");
		lista2.forEach(System.out::println);
	}

	private static void testGUI() {
		FurtherApp app = new FurtherApp();
		app.setWidth(1200);
		app.setHeight(900);
		app.run((xd) -> {
			new GUITest();
		});
	}

}
