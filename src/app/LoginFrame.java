package app;

import java.awt.BorderLayout;
import java.awt.Font;
import java.sql.SQLException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import session.Session;

public class LoginFrame extends BaseFrame {
	
	public LoginFrame() {
		super("�α���", 320, 160);
		
		setBorderLayout();
		addPrevForm(() -> new MainFrame().setVisible(true));
		
		east.setLayout(new BorderLayout());
		
		north.add(createLabel(new JLabel("�Ƹ�����Ʈ"), new Font("HY������M", 1, 24)));
		
		var tfId = new JTextField(12);
		var tfPw = new JPasswordField(12);
		
		center.add(createComp(new JLabel("���̵�"), 60, 30));
		center.add(tfId);
		center.add(createComp(new JLabel("��й�ȣ"), 60, 30));
		center.add(tfPw);
		
		east.add(createButton("�α���", e -> {
			if (tfId.getText().length() == 0 || tfPw.getText().length() == 0 ) {
				eMsg("��ĭ�� �����մϴ�.");
				return;
			}
			
			if (tfId.getText().equals("admin") && tfPw.getText().equals("1234")) {
				disposewithRemovingPrevForm();
				iMsg("�����ڷ� �α����Ͽ����ϴ�.");
				
				new AdminMainFrame().setVisible(true);
				return;
			}
			
			try {
				var rs = getPreparedResultSet("SELECT * FROM user WHERE binary u_id = ? AND binary u_pw = ?",
						tfId.getText(), tfPw.getText());
				
				if (rs.next()) {
					dispose();
					// �α��� ����
					Session.userNo = rs.getInt("u_no");
					Session.userName = rs.getString("u_name");
					Session.userImg = ImageIO.read(rs.getBlob("u_img").getBinaryStream());
					Session.userGender = rs.getInt("u_gender");
					Session.userGraduate = rs.getInt("u_graduate");
					iMsg(Session.userName + "�� ȯ���մϴ�.");
				} else {
					// ����
					eMsg("ȸ�� ������ ��ġ���� �ʽ��ϴ�.");
					tfId.setText("");
					tfPw.setText("");
					tfId.grabFocus();
				}
				
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}));
		
	}
	
	public static void main(String[] args) {
		
		
		new LoginFrame().setVisible(true);
	}

}
