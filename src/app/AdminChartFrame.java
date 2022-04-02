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

public class AdminChartFrame extends BaseFrame {
	int[] data = {0, 0, 0, 0, 0};
	JComboBox combo = new JComboBox();
	
	public AdminChartFrame() {
		super("지원자 분석", 600, 400);
		
		addPrevForm(() -> new AdminMainFrame().setVisible(true));
		setBorderLayout();
		
		north.add(createComp(
				createLabel(new JLabel("회사별 지원자 (연령별)", 0), new Font("HY헤드라인M", 1, 24))
				, 350, 40));
		
		
		north.add(combo);
		
		var rs = getPreparedResultSet("SELECT * FROM company");
		
		try {
			while (rs.next()) {
				combo.addItem(new ComboItem(rs.getInt("c_no"), rs.getString("c_name")));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		center.setLayout(new BorderLayout());
		
		Color[] colorList = { Color.BLACK, Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW };
		int maxBar = 250;
		
		var chart = new JPanel() {
			protected void paintComponent(java.awt.Graphics g) {
				
				g.setFont(new Font("굴림", 1, 12));
				
				int maxValue = Arrays.stream(data).max().getAsInt();
				
				for (int i = 0; i < 5; i++) {
					float percent = (float) data[i] / maxValue;
					int x = 25 + i * 100;
					int y = 10 + (int) (maxBar * (1f - percent));
					int w = 40;
					int h = (int) (maxBar * percent);
					
					g.setColor(colorList[i]);
					g.fillRect(x, y, w, h);
					g.fillRect(490, 70 + i * 35, 12, 12);
					
					g.setColor(Color.BLACK);
					g.drawRect(x, y, w, h);
					
					String age = String.format("%d0대", i + 1);
					
					g.drawString(age, 30 + i * 100, 280);
					g.drawString(String.format("%s:%d명", age, data[i]), 510, 80 + i * 35);
				}
				
			};
		};
		
		combo.addActionListener(e -> {
			updateChart(((ComboItem) combo.getSelectedItem()).id);
		});
		
		center.add(chart);
		
		updateChart(1);
	}
	
	void updateChart(int companyNo) {
		var rs = getPreparedResultSet("SELECT\r\n"
				+ "	COUNT(IF(year(now()) - year(u_birth) BETWEEN 10 AND 19, 1, null)) cnt1,\r\n"
				+ "    COUNT(IF(year(now()) - year(u_birth) BETWEEN 20 AND 29, 1, null)) cnt2,\r\n"
				+ "    COUNT(IF(year(now()) - year(u_birth) BETWEEN 30 AND 39, 1, null)) cnt3,\r\n"
				+ "    COUNT(IF(year(now()) - year(u_birth) BETWEEN 40 AND 49, 1, null)) cnt4,\r\n"
				+ "    COUNT(IF(year(now()) - year(u_birth) BETWEEN 50 AND 59, 1, null)) cnt5\r\n"
				+ "FROM company c\r\n"
				+ "INNER JOIN employment e ON c.c_no = e.c_no\r\n"
				+ "INNER JOIN applicant a ON e.e_no = a.e_no\r\n"
				+ "INNER JOIN user u ON a.u_no = u.u_no\r\n"
				+ "WHERE c.c_no = " + companyNo);
		
		try {
			rs.next();

			for (int i = 0; i < 5; i++) {
				data[i] = rs.getInt(i + 1);
			}
			
			int sum = Arrays.stream(data).sum();
			
			if (sum > 0) {
				repaint();
			} else {
				eMsg("지원자 또는 공고가 없습니다.");
				
				if (companyNo != 1) {
					combo.setSelectedIndex(0);
					updateChart(1);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new AdminChartFrame().setVisible(true);
	}

}
