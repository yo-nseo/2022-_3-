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

public class AdminEmploymentFrame extends BaseFrame {
	JTextField[] tfList = {
		new JTextField(), new JTextField(), new JTextField()
	};
	
	JComboBox[] cbList = {
		new JComboBox(), new JComboBox(grad)	
	};
	JRadioButton[] rdoList = {
		new JRadioButton("남", true), new JRadioButton("여"), new JRadioButton("무관")
	};
	int empNo;
	JButton btnDelete;
	
	public AdminEmploymentFrame(int empNo) {
		super("공고수정", 450, 400);
		
		this.empNo = empNo;
		
		setBorderLayout();
		center.setLayout(null);
		center.setBorder(new TitledBorder(new LineBorder(Color.BLACK), "모집내용"));
		south.setLayout(new FlowLayout(FlowLayout.RIGHT));
		
		var lbList = "회사명,공고내용,시급,모집정원,성별,최종학력".split(",");
		var btnGroup = new ButtonGroup();
		
		Arrays.stream(rdoList)
			.forEach(r -> btnGroup.add(r));
		
		for (int i = 0; i < lbList.length; i++) {
			center.add(createComp(new JLabel(lbList[i]), 15, 30 + i * 45, 100, 30));
			
			if (i == 0) {
				// 회사명
				center.add(createComp(cbList[0], 100, 30 + i * 45, 130, 30));
			} else if (i < 4) {
				// 공고내용 ~ 모집정원
				center.add(createComp(tfList[i - 1], 100, 30 + i * 45, 280, 30));
			} else if (i < 5) {
				// 성별
				center.add(createComp(rdoList[0], 100, 30 + i * 45, 50, 30));
				center.add(createComp(rdoList[1], 150, 30 + i * 45, 50, 30));
				center.add(createComp(rdoList[2], 200, 30 + i * 45, 50, 30));
			} else {
				// 최종 학력
				center.add(createComp(cbList[1], 100, 30 + i * 45, 100, 30));
			}
		}
		
		if (empNo == 0) {
			// 등록 모드
			setTitle("공고등록");
			
			// 공고 없는 회사 리스트
			var rs = getPreparedResultSet("SELECT *\r\n"
					+ "FROM 2022지방_2.company c\r\n"
					+ "WHERE NOT EXISTS (SELECT * FROM employment e WHERE c.c_no = e.c_no );");
			
			try {
				while (rs.next()) {
					cbList[0].addItem(new ComboItem(rs.getInt("c_no"), rs.getString("c_name")));
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			
		} else {
			// 수정 모드
			// 다른 공고 회사명 콤보박스
			try (var rs = getPreparedResultSet("SELECT e.*, c.*, (SELECT COUNT(1) FROM applicant a WHERE a.e_no = e.e_no AND a.a_apply < 2) AS cnt\r\n"
					+ "FROM employment e\r\n"
					+ "INNER JOIN company c ON e.c_no = c.c_no "
					+ "HAVING cnt < e_people")) {
				while (rs.next()) {
					cbList[0].addItem(new ComboItem(rs.getInt("e_no"), rs.getString("c_name")));
					
					if (rs.getInt("e_no") == empNo) {
						cbList[0].setSelectedIndex(cbList[0].getItemCount() - 1);
					}
					
				}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			
			cbList[0].addActionListener(e -> {
				this.empNo = ((ComboItem) cbList[0].getSelectedItem()).id;
				updateForm();
			});	
		}
		
		south.add(createComp(createButton(empNo == 0 ? "등록" : "수정", e -> {
			if (Arrays.stream(tfList)
				.anyMatch(tf -> tf.getText().length() == 0)) {
				eMsg("빈칸이 존재합니다.");
				return;
			}
			
			// 숫자 여부
			try {
				Integer.parseInt(tfList[1].getText());
				Integer.parseInt(tfList[2].getText());					
			} catch (NumberFormatException e1) {
				eMsg("숫자로 입력하세요");
				return;
			}

			int eGender = 1;
			
			if (rdoList[1].isSelected())
				eGender = 2;
			else if (rdoList[2].isSelected())
				eGender = 3;
			
			if (empNo == 0) {
				// 등록
				executeSQL("INSERT INTO employment VALUES(0, ?, ?, ?, ?, ?, ?)", 
						((ComboItem) cbList[0].getSelectedItem()).id,
						tfList[0].getText(),
						tfList[1].getText(),
						tfList[2].getText(),
						eGender,
						cbList[1].getSelectedIndex());
					
				iMsg("등록이 완료되었습니다.");
				dispose();
			} else {
				// 수정
				executeSQL("UPDATE employment SET e_title = ?, e_pay = ?, e_people = ? WHERE e_no = ?", 
						tfList[0].getText(),
						tfList[1].getText(),
						tfList[2].getText(),
						empNo);
					
				iMsg("수정이 완료되었습니다.");
				dispose();
			}
			
			
		}), 100, 30));
		
		south.add(btnDelete = createComp(createButton("삭제", e -> {
			dispose();
			iMsg("삭제가 완료되었습니다.");
			executeSQL("DELETE FROM employment WHERE e_no = " + empNo);
		}), 100, 30));
		
		btnDelete.setVisible(false);
		
		// 수정모드 기존 데이터 채우기
		if (empNo != 0)
			updateForm();
	}
	
	void updateForm()
	{
		try (var rs = getPreparedResultSet("SELECT e.*, c.*, (SELECT COUNT(1) FROM applicant a WHERE a.e_no = e.e_no AND a.a_apply < 2) AS cnt\r\n"
				+ "FROM employment e\r\n"
				+ "INNER JOIN company c ON e.c_no = c.c_no "
				+ "WHERE e.e_no = ? "
				+ "HAVING cnt < e_people", empNo);) {
			
			rs.next();
			
			tfList[0].setText(rs.getString("e_title"));
			tfList[1].setText(rs.getString("e_pay"));
			tfList[2].setText(rs.getString("e_people"));
			rdoList[rs.getInt("e_gender") - 1].setSelected(true);
			cbList[1].setSelectedIndex(rs.getInt("e_graduate"));
			cbList[1].setEnabled(false);
			
			Arrays.stream(rdoList)
				.forEach(r -> r.setEnabled(false));
			
			// 지원자 없을 시 삭제 버튼 추가
			btnDelete.setVisible(rs.getInt("cnt") == 0);
			
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new AdminEmploymentFrame(1).setVisible(true);
	}

}
