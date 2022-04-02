package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
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

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import session.Session;

public class AdminApplicantInfoFrame extends BaseFrame {
	JPopupMenu menu = new JPopupMenu();
	int selectedNo = -1;
	
	public AdminApplicantInfoFrame() {
		super("지원자 정보", 500, 400);
		
		addPrevForm(() -> new AdminMainFrame().setVisible(true));
		setBorderLayout();
		
		add(new JScrollPane(center));

		center.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
		
		JMenuItem[] menuItems = {
				new JMenuItem("합격"),
				new JMenuItem("불합격")
		};
		
		menu.add(menuItems[0]);
		menu.add(menuItems[1]);
		
		menuItems[0].addActionListener(e -> {
			// 합격 처리
			executeSQL("UPDATE applicant SET a_apply = 1 WHERE a_no = " + selectedNo);
			iMsg("심사가 완료되었습니다.");
			updateList();
		});
		
		menuItems[1].addActionListener(e -> {
			// 불합격 처리
			executeSQL("UPDATE applicant SET a_apply = 2 WHERE a_no = " + selectedNo);
			iMsg("심사가 완료되었습니다.");
			updateList();
		});
		
		updateList();
	}
	
	void updateList() {
		center.removeAll();
		
		var rs = getPreparedResultSet("SELECT a_no, u_name, c_name, u_birth, u_graduate, u_email, u_img\r\n"
				+ "FROM applicant a\r\n"
				+ "INNER JOIN user u ON a.u_no = u.u_no\r\n"
				+ "INNER JOIN employment e ON a.e_no = e.e_no\r\n"
				+ "INNER JOIN company c ON e.c_no = c.c_no\r\n"
				+ "WHERE a_apply = 0;");
		
		try {
			int cnt = 0;
			
			while (rs.next()) {
				var panel = new JPanel(null);
				final int aNo = rs.getInt("a_no");
				
				panel.setBorder(new LineBorder(Color.BLACK));
				panel.setPreferredSize(new Dimension(450, 160));
				
				var img = createLabel(ImageIO.read(rs.getBlob("u_img").getBinaryStream()), 140, 140);
				img.setBorder(new EmptyBorder(0, 0, 0, 0));
				panel.add(createComp(img, 10, 10, 140, 140));
				
				panel.add(createComp(
						createLabel(new JLabel("지원 회사 : " + rs.getString("c_name")), new Font("HY헤드라인M", 1, 18))
						, 160, 10, 280, 30));
				
				int age = 10;
				
				panel.add(createComp(
						createLabel(new JLabel("이름 : " + rs.getString("u_name") + "(나이 : " + age + "세)"), new Font("굴림", 1, 14))
						, 170, 40, 280, 30));
				
				panel.add(createComp(
						createLabel(new JLabel("생년월일 : " + rs.getString("u_birth")), new Font("굴림", 1, 14))
						, 170, 70, 280, 30));
				
				panel.add(createComp(
						createLabel(new JLabel("최종학력 : " + grad[rs.getInt("u_graduate")]), new Font("굴림", 1, 14))
						, 170, 100, 280, 30));
				
				panel.add(createComp(
						createLabel(new JLabel("이메일 : " + rs.getString("u_email")), new Font("굴림", 1, 14))
						, 170, 130, 280, 30));
				
				panel.addMouseListener(new MouseAdapter() {
					
					@Override
					public void mouseReleased(MouseEvent e) {
						if (e.getButton() == MouseEvent.BUTTON3) {
							selectedNo = aNo;
							menu.show(panel, e.getX(), e.getY());
						}
					}
				});
				
				center.add(panel);
				++cnt;
			}
			
			center.setPreferredSize(new Dimension(450, cnt * 160));
			center.revalidate();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new AdminApplicantInfoFrame().setVisible(true);
	}

}
