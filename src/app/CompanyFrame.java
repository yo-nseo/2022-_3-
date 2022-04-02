package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import session.Session;

public class CompanyFrame extends BaseFrame {
	
	
	public CompanyFrame(int companyNo, boolean editMode) {
		super(editMode ? "�����������" : "���������", 250, 400);
		
		setBorderLayout();
		south.setLayout(new BorderLayout());
		north.setLayout(new BorderLayout());
		
		var rs = getPreparedResultSet("SELECT c_name, c_ceo, c_address, c_category, c_employee, c_img FROM company WHERE c_no = " + companyNo);
		
		try {
			rs.next();
			
			var lbImg = createLabel(ImageIO.read(rs.getBlob("c_img").getBinaryStream()), 240, 160);
			
			north.add(lbImg);
			
			var lbList = "�����,��ǥ��,�ּ�,����,������".split(",");
			var tfList = new JTextField[lbList.length];
			
			for (int i = 0; i < lbList.length; i++) {
				center.add(createComp(new JLabel(lbList[i]), 80, 24));
				center.add(tfList[i] = new JTextField(12));
				tfList[i].setEditable(false);
				
				if (i == 3)
					tfList[i].setText(noToStr(rs.getString(i + 1)));					
				else
					tfList[i].setText(rs.getString(i + 1));
			}
			
			north.setBorder(new EmptyBorder(8, 8, 8, 8));
			south.setBorder(new EmptyBorder(8, 8, 8, 8));
			
			// �������
			// �̹��� ����Ŭ�� ����
			// ��ǥ��, �ּ� �Է�
			// �������� ���
			if (editMode) {
				tfList[1].setEditable(true);
				tfList[2].setEditable(true);
				
				var chooser = new JFileChooser();
				
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPG Images", "jpg"));
				
				lbImg.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						
						if (e.getClickCount() == 2) {
							// ���� Ŭ�� �� �̹��� ����
							chooser.showOpenDialog(null);
							
							if (chooser.getSelectedFile() != null) {
								try {
									lbImg.setIcon(getResizedIcon(ImageIO.read(chooser.getSelectedFile()) , 240, 160));
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
						
					}
				});
				
				tfList[3].addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						// ���� ����
						setVisible(false);
						var skillsFrame = new SelectSkillsFrame(strToNo(tfList[3].getText()));
						
						skillsFrame.btnSelect.addActionListener(e1 -> {
							
							if (skillsFrame.getCategoryStr().length() == 0)
								eMsg("������ �����ϼ���.");
							else
								skillsFrame.dispose();
								tfList[3].setText(skillsFrame.getCategoryStr());
						});
						
						skillsFrame
							.addPrevForm(() -> CompanyFrame.this.setVisible(true))
							.setVisible(true);
					}
				});
				
				south.add(createButton("����", e -> {
					
					if (Arrays.stream(tfList)
							.anyMatch(tf -> tf.getText().length() == 0)) {
						eMsg("��ĭ�� �����մϴ�.");
						return;
					}
					
					
					try {
						executeSQL("UPDATE company SET c_ceo = ?, c_address = ?, c_category = ? WHERE c_no = ?",
								tfList[1].getText(), tfList[2].getText(), strToNo(tfList[3].getText()), companyNo);
						
						if (chooser.getSelectedFile() != null) {
							// �̹��� ����
							Files.copy(chooser.getSelectedFile().toPath(),
								Paths.get("datafiles/���/" + tfList[0].getText() + "1.jpg"),
								// ���� ���� �� �����
								StandardCopyOption.REPLACE_EXISTING);
							
							// DB �̹��� ����
							executeSQL("UPDATE company SET c_img = ? WHERE c_no = ?",
									new FileInputStream(chooser.getSelectedFile()), companyNo);
						}
						
						iMsg("������ �Ϸ�Ǿ����ϴ�.");
						dispose();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}));
			} else {
				south.add(createButton("�ݱ�", e -> dispose()));							
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// admin ��� �׽�Ʈ
		new CompanyFrame(10, true).setVisible(true);
//		new CompanyFrame(1, false).setVisible(true);
	}

}
