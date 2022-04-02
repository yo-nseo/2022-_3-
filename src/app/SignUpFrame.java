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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;

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
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import session.Session;

public class SignUpFrame extends BaseFrame {
	
	
	public SignUpFrame() {
		super("ȸ������", 500, 500);
		
		setBorderLayout();
		addPrevForm(() -> new MainFrame().setVisible(true));
		
		center.setLayout(null);
		center.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "ȸ������"));
		south.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		var lbList = "�̸�,���̵�,��й�ȣ,�������,�̸���,����,�����з�,�ּ�".split(",");
		var tfList = new JTextField[6];
		var lbImg = createLabel(new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB), 150, 150);
		var btnGroup = new ButtonGroup();		
		JRadioButton[] rdoList = {
			new JRadioButton("��", true),
			new JRadioButton("��")
		};
		
		btnGroup.add(rdoList[0]);
		btnGroup.add(rdoList[1]);

		JComboBox[] cbList = {
			new JComboBox("naver.com,outlook.com,daum.com,gmail.com,nate.com,kebi.com,yahoo.com,korea.com,empal.com,hanmail.net".split(",")),
			new JComboBox(",���б� ����,����б� ����,���б� ����".split(",")),
			new JComboBox(",����,�λ�,�뱸,��õ,����,����,���,����,���,����,���,�泲,����,����,���,�泲,����".split(","))
		};
		
		for (int i = 0; i < 6; i++) {
			tfList[i] = new JTextField();
		}
		
		tfList[5].setEditable(false);
	
		for (int i = 0; i < lbList.length; i++) {
			center.add(createComp(new JLabel(lbList[i]), 8, 30 + i * 46, 100, 30));
			
			if (i == 0) {
				center.add(createComp(lbImg, 300, 30 + i * 46, 150, 150));
			}
			
			if (i < 4) {
				center.add(createComp(tfList[i], 108, 30 + i * 46, 150, 24));
			} else if (i < 5) {
				center.add(createComp(tfList[i], 108, 30 + i * 46, 80, 24));
				center.add(createComp(new JLabel("@"), 190, 30 + i * 46, 24, 24));
				center.add(createComp(cbList[0], 210, 30 + i * 46, 100, 24));
			} else if (i < 6) {
				center.add(createComp(rdoList[0], 108, 30 + i * 46, 40, 24));
				center.add(createComp(rdoList[1], 150, 30 + i * 46, 40, 24));
			} else if (i < 7) {
				center.add(createComp(cbList[1], 108, 30 + i * 46, 100, 24));
			} else {
				center.add(createComp(cbList[2], 108, 30 + i * 46, 100, 24));
				center.add(createComp(tfList[5], 220, 30 + i * 46, 180, 24));
			}
		}

		var chooser = new JFileChooser();
		
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("JPG Images", "jpg"));
		
		lbImg.setIcon(null);
		lbImg.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					
					chooser.showOpenDialog(null);
					
					if (chooser.getSelectedFile() != null) {
						try {
							var img = ImageIO.read(chooser.getSelectedFile());
							
							lbImg.setIcon(new ImageIcon(img.getScaledInstance(150, 150, BufferedImage.TYPE_INT_RGB)));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		});
		
		cbList[2].addActionListener(e -> {
			
			if (cbList[2].getSelectedItem().equals("")) {
				tfList[5].setText("");
				tfList[5].setEditable(false);
			} else {
				tfList[5].setEditable(true);
				tfList[5].grabFocus();
			}
			
		});
		
		south.add(createComp(createButton("����", e -> {
			
			if (	// tf ��ĭ
					Arrays.stream(tfList).anyMatch(tf -> tf.getText().length() == 0) ||
					// cb ��ĭ
					Arrays.stream(cbList).anyMatch(cb -> cb.getSelectedItem().equals("")) ||
					// img ��ĭ
					chooser.getSelectedFile() == null
					) {
				eMsg("��ĭ�� �����մϴ�.");
				return;
			}
			
			
			try {
				var rs = getPreparedResultSet("SELECT * FROM user WHERE u_id = ?", tfList[1].getText());
				
				if (rs.next()) {
					eMsg("�̹� �����ϴ� ���̵��Դϴ�.");
					return;
				}
				
				String pw = tfList[2].getText();
				
				// 4�ڸ� �̸�
				if (pw.length() < 4 ||
					// ���� �Ǵ� ������ ����
					!pw.matches(".*[a-zA-Z].*") ||
					!pw.matches(".*[0-9].*") || 
					!pw.matches(".*[!@#$].*")) {
					eMsg("��й�ȣ ������ ��ġ���� �ʽ��ϴ�. ");
					return;
				}
				
				// ������� �˻�
				try {
					var date = sdf.parse(tfList[3].getText()); 
					
					if (date.compareTo(new Date()) > 0) {
						throw new ParseException("", 0);
					}					
				} catch (ParseException e1) {
					eMsg("������� ������ ���� �ʽ��ϴ�.");
					tfList[3].setText("");
					tfList[3].grabFocus();
					return;
				} 
				
				// ȸ������
				int id = executeSQL("INSERT INTO user VALUES (0, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 
						tfList[0].getText(), tfList[1].getText(), tfList[2].getText(), tfList[3].getText(), 
						String.format("%s@%s", tfList[4].getText(), cbList[0].getSelectedItem()),
						rdoList[0].isSelected() ? 1 : 2,
						cbList[1].getSelectedIndex() - 1,
						String.format("%s %s", cbList[2].getSelectedItem(), tfList[5].getText()),
						new FileInputStream(chooser.getSelectedFile()));
				
				// �̹��� ���� ����
				Files.copy(chooser.getSelectedFile().toPath(), Paths.get("./datafiles/ȸ������/" + id + ".jpg"));
				
				iMsg("ȸ�������� �Ϸ�Ǿ����ϴ�.");
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			
		}), 100, 30));
		
	}
	
	public static void main(String[] args) {
		new SignUpFrame().setVisible(true);
	}

}
