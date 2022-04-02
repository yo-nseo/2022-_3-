package app;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

public class SelectSkillsFrame extends BaseFrame {
	private ArrayList<String> strList = new ArrayList<String>();
	public JButton btnSelect = new JButton("����");
	
	public SelectSkillsFrame(String category) {
		super("��������", 300, 350);
		
		setBorderLayout();
		
		var border = new TitledBorder(new LineBorder(Color.BLACK), "��������");
		
		border.setTitleFont(new Font("����", 1, 18));
		center.setBorder(border);
		center.setLayout(new GridLayout(4, 2));
		
		var checkList = new JCheckBox[categoryList.length];
		var tf = new JTextField(15);
		
		tf.setEnabled(false);
		tf.setEditable(false);
		
		for (int i = 0; i < categoryList.length; i++) {
			final int id = i;
			final String strId = String.format("%d", id + 1);
			
			center.add(checkList[id] = new JCheckBox(categoryList[id]));
			
			checkList[id].addActionListener(e -> {
				if (checkList[id].isSelected()) {
					strList.add(categoryList[id]);
				} else {
					strList.remove(categoryList[id]);
				}
				
				tf.setText(String.join(",", strList));
			});
		}
		
		if (category.length() > 0) {
			for (String c : category.split(",")) {
				int no = Integer.valueOf(c) - 1;
				
				checkList[no].doClick();
			}			
		}
		
		createComp(south, 0, 75);
		south.add(createLabel(new JLabel("����������"), new Font("����", 1, 18)));
		south.add(tf);
		south.add(createComp(btnSelect, 100, 28));
		
	}
	
	public String getCategoryStr() {
		return String.join(",", strList);
	}
	
	public static void main(String[] args) {
		new SelectSkillsFrame("2,3").setVisible(true);
	}
}
