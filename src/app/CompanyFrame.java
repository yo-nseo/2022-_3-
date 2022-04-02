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
		super(editMode ? "기업정보수정" : "기업상세정보", 250, 400);
		
		setBorderLayout();
		south.setLayout(new BorderLayout());
		north.setLayout(new BorderLayout());
		
		var rs = getPreparedResultSet("SELECT c_name, c_ceo, c_address, c_category, c_employee, c_img FROM company WHERE c_no = " + companyNo);
		
		try {
			rs.next();
			
			var lbImg = createLabel(ImageIO.read(rs.getBlob("c_img").getBinaryStream()), 240, 160);
			
			north.add(lbImg);
			
			var lbList = "기업명,대표자,주소,직종,직원수".split(",");
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
			
			// 수정모드
			// 이미지 더블클릭 수정
			// 대표자, 주소 입력
			// 직종선택 기능
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
							// 더블 클릭 시 이미지 수정
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
						// 직종 선택
						setVisible(false);
						var skillsFrame = new SelectSkillsFrame(strToNo(tfList[3].getText()));
						
						skillsFrame.btnSelect.addActionListener(e1 -> {
							
							if (skillsFrame.getCategoryStr().length() == 0)
								eMsg("직종을 선택하세요.");
							else
								skillsFrame.dispose();
								tfList[3].setText(skillsFrame.getCategoryStr());
						});
						
						skillsFrame
							.addPrevForm(() -> CompanyFrame.this.setVisible(true))
							.setVisible(true);
					}
				});
				
				south.add(createButton("수정", e -> {
					
					if (Arrays.stream(tfList)
							.anyMatch(tf -> tf.getText().length() == 0)) {
						eMsg("빈칸이 존재합니다.");
						return;
					}
					
					
					try {
						executeSQL("UPDATE company SET c_ceo = ?, c_address = ?, c_category = ? WHERE c_no = ?",
								tfList[1].getText(), tfList[2].getText(), strToNo(tfList[3].getText()), companyNo);
						
						if (chooser.getSelectedFile() != null) {
							// 이미지 복사
							Files.copy(chooser.getSelectedFile().toPath(),
								Paths.get("datafiles/기업/" + tfList[0].getText() + "1.jpg"),
								// 파일 존재 시 덮어쓰기
								StandardCopyOption.REPLACE_EXISTING);
							
							// DB 이미지 변경
							executeSQL("UPDATE company SET c_img = ? WHERE c_no = ?",
									new FileInputStream(chooser.getSelectedFile()), companyNo);
						}
						
						iMsg("수정이 완료되었습니다.");
						dispose();
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				}));
			} else {
				south.add(createButton("닫기", e -> dispose()));							
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// admin 모드 테스트
		new CompanyFrame(10, true).setVisible(true);
//		new CompanyFrame(1, false).setVisible(true);
	}

}
