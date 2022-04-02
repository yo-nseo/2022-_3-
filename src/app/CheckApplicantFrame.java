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

public class CheckApplicantFrame extends BaseFrame {
	
	public JButton btnCheck = new JButton("지원가능여부보기");
	
	public CheckApplicantFrame(int empNo) {
		super("지원가능여부", 300, 270);
		
		setBorderLayout();
		
		var lbList = "기업이름,대표자,주소,모집성별,모집최종학력".split(",");
		var tfList = new JTextField[lbList.length];
				
		try (var rs = getPreparedResultSet(
				"SELECT c_name, c_ceo, c_address, e_gender, e_graduate, c_img\r\n"
				+ "FROM employment e\r\n"
				+ "INNER JOIN company c ON e.c_no = c.c_no\r\n"
				+ "WHERE e_no = " + empNo)) {
			
			rs.next();
			setIconImage(ImageIO.read(rs.getBlob("c_img").getBinaryStream()));
			
			for (int i = 0; i < lbList.length; i++) {
				center.add(createComp(new JLabel(lbList[i]), 100, 30));
				center.add(tfList[i] = new JTextField(14));
				
				tfList[i].setEditable(false);
				
				if (i < 3) {
					tfList[i].setText(rs.getString(i + 1));
				} else if (i < 4) {
					tfList[i].setText(gender[rs.getInt(i + 1)]);
				} else {
					tfList[i].setText(grad[rs.getInt(i + 1)]);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		south.add(btnCheck);
	}
	
	public static void main(String[] args) {
		Session.userNo = 1;
		Session.userName = "이솜";
		Session.userGender = 2;
		Session.userGraduate = 2;
		
		new CheckApplicantFrame(7).setVisible(true);
	}

}
