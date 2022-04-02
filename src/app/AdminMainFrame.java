package app;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import session.Session;

public class AdminMainFrame extends BaseFrame {
	
	String focusName;
	
	public AdminMainFrame() {
		super("관리자 메인", 600, 620);
		
		setBorderLayout();
		addPrevForm(() -> new LoginFrame().setVisible(true));
		
		var rs = getPreparedResultSet("SELECT * FROM company");
		
		try {
			while (rs.next()) {
				int companyNo = rs.getInt("c_no");
				String name = rs.getString("c_name");
				var lbImg = createComp(new JLabel("") {
					
					@Override
					protected void paintComponent(Graphics g) {
						Graphics2D g2d = (Graphics2D) g;
						
						g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
								focusName == name ? 1 : 0.2f));
						
						super.paintComponent(g);
					}
					
				}, 100, 100);
				
				lbImg.setIcon(getResizedIcon(ImageIO.read(rs.getBlob("c_img").getBinaryStream()), 100, 100));
				lbImg.setBorder(new LineBorder(Color.BLACK));
				lbImg.setToolTipText(rs.getString("c_name"));
				
				lbImg.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseEntered(MouseEvent e) {
						focusName = name;
						lbImg.repaint();
					}
					
					@Override
					public void mouseExited(MouseEvent e) {
						focusName = null;
						lbImg.repaint();
					}
					
					@Override
					public void mouseClicked(MouseEvent e) {
						disposewithRemovingPrevForm();
						new CompanyFrame(companyNo, true)
							.addPrevForm(() -> new AdminMainFrame().setVisible(true))
							.setVisible(true);
					}
				});
				
				center.add(lbImg);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		south.add(createButton("채용 정보", e -> {
			disposewithRemovingPrevForm();
			new AdminJobInfoFrame().setVisible(true);
		}));
		south.add(createButton("지원자 목록", e -> {
			disposewithRemovingPrevForm();
			new AdminApplicantInfoFrame().setVisible(true);
		}));
		south.add(createButton("공고 등록", e -> {
			disposewithRemovingPrevForm();
			new AdminEmploymentFrame(0)
				.addPrevForm(() -> new AdminMainFrame().setVisible(true))
				.setVisible(true);
		}));
		south.add(createButton("지원자 분석", e -> {
			disposewithRemovingPrevForm();
			new AdminChartFrame().setVisible(true);
		}));
		south.add(createButton("닫기", e -> dispose()));
	}
	
	public static void main(String[] args) {
		new AdminMainFrame().setVisible(true);
	}

}
