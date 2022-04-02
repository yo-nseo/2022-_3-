package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import session.Session;

public class MainFrame extends BaseFrame {
	JLabel[] lbCompany = new JLabel[5];
	JLabel[] lbCount= new JLabel[5];
	JPanel animationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
	JComboBox cbProvince = new JComboBox("��ü,����,�λ�,�뱸,��õ,����,����,���,����,���,����,���,�泲,����,����,���,�泲,����".split(","));
	
	public MainFrame() {
		super("Main", 320, 420);
		
		setBorderLayout();
		
		north.add(createLabel(new JLabel("�Ƹ�����Ʈ"), new Font("HY������M", 1, 24)));
		center.setLayout(new BorderLayout());
		
		var centerNorth = new JPanel();
		var centerCenter = new JPanel(null);
		var tfSearch = new JTextField(12);
		
		centerNorth.add(new JLabel("����˻�"));
		centerNorth.add(tfSearch);
		centerNorth.add(createButton("�˻�", e -> {
			
			if (tfSearch.getText().length() == 0) {
				eMsg("�˻��� ������� �Է��ϼ���.");
				return;
			}
			
			String keyword = "%" + tfSearch.getText() + "%";
			var rs = getPreparedResultSet("SELECT * FROM company WHERE c_name LIKE ?", keyword);
			
			try {
				if (rs.next()) {
					// c_search ����
					executeSQL("UPDATE company SET c_search = c_search + 1 WHERE c_no = " + rs.getInt("c_no"));
					
					// �Ǽ� ������Ʈ
					updateSearch();
					
					// �󼼱������ �� ����
					setVisible(false);
					new CompanyFrame(rs.getInt("c_no"), false)
						.addPrevForm(() -> setVisible(true))
						.setVisible(true);
					
				} else {
					eMsg("�˻��� ����� �����ϴ�.");
					tfSearch.setText("");
					tfSearch.grabFocus();
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
		}));
		
		centerCenter.add(createComp(new JLabel("�α���"), 10, 0, 80, 24));
		
		for (int i = 1; i <= 5; i++) {
			centerCenter.add(createComp(new JLabel(i + ""), 10, i * 28, 20, 24));
			centerCenter.add(lbCompany[i-1] = createComp(new JLabel("���"), 40, i * 28, 80, 24));
			centerCenter.add(lbCount[i-1] = createComp(new JLabel("0"), 130, i * 28, 30, 24));
		}
		
		if (Session.userNo == 0) {
			// �α���X
			centerCenter.add(createComp(createButton("�α���", e -> {
				dispose();
				
				new LoginFrame()
					.setVisible(true);
			})
					, 170, 28, 100, 36));
			centerCenter.add(createComp(createButton("ȸ������", e -> {
				dispose();
				
				new SignUpFrame()
					.setVisible(true);
			})
					, 170, 28 + 40, 100, 36));
			centerCenter.add(createComp(createButton("�ݱ�", e -> dispose())
					, 170, 28 + 80, 100, 36));
		} else {
			// �α��ε�
			north.add(createLabel(Session.userImg, 30, 30));
			north.add(new JLabel(Session.userName + "�� ȯ���մϴ�."));
			
			centerCenter.add(createComp(createButton("�α׾ƿ�", e -> {
				dispose();
				Session.userNo = 0;
				Session.userName = "";
				new MainFrame().setVisible(true);
			})
					, 170, 28, 100, 36));
			centerCenter.add(createComp(createButton("ä������", e -> {
				dispose();
				new JobInfoFrame()
					.setVisible(true);
			})
					, 170, 28 + 40, 100, 36));
			centerCenter.add(createComp(createButton("����������", e -> {
				dispose();
				new MyPageFrame().setVisible(true);
			})
					, 170, 28 + 80, 100, 36));
			centerCenter.add(createComp(createButton("�ݱ�", e -> dispose())
					, 170, 28 + 120, 100, 36));
		}
		
		center.add(centerNorth, BorderLayout.NORTH);
		center.add(centerCenter);
		
		centerCenter.add(createComp(new JLabel("����"), 10, 180, 80, 24));
		centerCenter.add(createComp(cbProvince, 40, 180, 120, 24));
		
		var wrapper = createComp(new JPanel(null), 5, 210, 284, 74);
		
		wrapper.add(createComp(animationPanel, 1, 1, 0, 0));
		
		centerCenter.add(wrapper);
		
		updateSearch();
		createAnimation("");
		
		cbProvince.addActionListener(e -> {
			createAnimation(cbProvince.getSelectedItem().toString());
		});
	}
	
	private void createAnimation(String area)
	{
		destroyThread();
		
		animationPanel.removeAll();
		
		area = area.equals("��ü") ? "" : area;
		
		var rs = getPreparedResultSet("SELECT * FROM company WHERE c_address LIKE '" + area + "%'");
		var nameList = new ArrayList<String>();
		var imgList = new ArrayList<ImageIcon>();
		
		try {			
			while (rs.next()) {
				nameList.add(rs.getString("c_name"));
				imgList.add(getResizedIcon(ImageIO.read(rs.getBlob("c_img").getBinaryStream()), 70, 70));
			}
			
			if (nameList.size() > 0) {
				
				for (int i = 0; i < imgList.size(); i++) {
					var panel = createComp(new JPanel(new BorderLayout()), 70, 70);
					
					panel.setBorder(new LineBorder(Color.BLACK));
					panel.add(new JLabel(imgList.get(i)));
					panel.add(new JLabel(nameList.get(i), JLabel.CENTER), BorderLayout.SOUTH);
					
					animationPanel.add(panel);
				}
				
				animationPanel.setBounds(1, 1, nameList.size() * 71, 71);			
			} else {
				eMsg("������ ��������� �����ϴ�.");
				cbProvince.setSelectedItem("��ü");
				createAnimation("");
				return;
			}
			
			animationPanel.validate();
			animationPanel.repaint();
			
			thread = new Thread( () -> {
				try {
					while (true) {
						animationPanel.setBounds(animationPanel.getBounds().x - 1, 1, nameList.size() * 71, 71);
						
						Thread.sleep(10);
					}
				} catch (InterruptedException e) { }
			});
			
			thread.start();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void updateSearch() {
		try {
			var rs = stmt.executeQuery("SELECT c_name, c_search\r\n"
					+ "FROM company\r\n"
					+ "ORDER BY c_search DESC, c_no\r\n"
					+ "LIMIT 5;");
			
			for (int i = 0; rs.next(); i++) {
				lbCompany[i].setText(rs.getString(1));
				lbCount[i].setText(rs.getString(2));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new MainFrame().setVisible(true);
	}

}
