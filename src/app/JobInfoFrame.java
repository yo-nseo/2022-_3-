package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import session.Session;

public class JobInfoFrame extends BaseFrame {
	DefaultTableModel dtm = new DefaultTableModel("no,�̹���,�����,��������,�ñ�,����,����,�з�,����".split(","), 0) {
		public boolean isCellEditable(int row, int column) {
			return false;
		};
	};
	JTextField[] tfList = {
		new JTextField(14), new JTextField(14)
	};
	JComboBox[] cbList = {
		new JComboBox("��ü,����,�λ�,�뱸,��õ,����,����,���,����,���,����,���,�泲,����,����,���,�泲,����".split(",")),
		new JComboBox("��ü,���б� ����,����б� ����,���б� ����,����".split(",")),
		new JComboBox("��ü,����,����,����".split(","))
	};
	
	
	public JobInfoFrame() {
		super("ä������", 800, 600);
		
		setBorderLayout();
		addPrevForm(() -> new MainFrame().setVisible(true));
		north.add(createLabel(new JLabel("ä������"), new Font("HY������M", 1, 24)));
		north.setBorder(new EmptyBorder(10, 10, 10, 10));
		center.setLayout(null);
		
		JPanel[] flow = {
				new JPanel(new FlowLayout(FlowLayout.LEFT) ),
				new JPanel(new FlowLayout(FlowLayout.LEFT) ),
				new JPanel(new FlowLayout(FlowLayout.LEFT) ),
				new JPanel(new FlowLayout(FlowLayout.LEFT) )
		};
		
		tfList[1].setEditable(false);
		
		flow[0].add(createComp(new JLabel("�����", 0), 60, 24));
		flow[0].add(tfList[0]);
		flow[1].add(createComp(new JLabel("����", 0), 60, 24));
		flow[1].add(tfList[1]);
		flow[2].add(createComp(new JLabel("����", 0), 60, 24));
		flow[2].add(cbList[0]);
		flow[2].add(createComp(new JLabel("�з�", 0), 60, 24));
		flow[2].add(cbList[1]);
		flow[2].add(createComp(new JLabel("����", 0), 60, 24));
		flow[2].add(cbList[2]);
		
		var table = new JTable(dtm);
		var btnSubmit = createButton("�����ϱ�", e -> {
			executeSQL("INSERT INTO applicant VALUES(0, ?, ?, 0)", 
					dtm.getValueAt(table.getSelectedRow(), 0),
					Session.userNo);
			
			iMsg("��û�� �Ϸ�Ǿ����ϴ�.");
			
			updateList(false);
		});
		
		btnSubmit.setEnabled(false);
		
		flow[3].add(createButton("�˻��ϱ�", e -> updateList(false)));
		flow[3].add(btnSubmit);
		
		center.add(createComp(flow[0], 10, 10, 450, 34));
		center.add(createComp(flow[1], 10, 40, 450, 34));
		center.add(createComp(flow[2], 10, 70, 450, 34));
		center.add(createComp(flow[3], 570, 70, 200, 40));
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.removeColumn(table.getColumn("no"));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		int[] widthList = {40, 140, 40, 40, 120, 120, 80, 40};
		
		table.setRowHeight(40);
		
		var cellRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				JLabel lb = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				
				if (column == 0) {
					lb.setText("");
					lb.setIcon(getResizedIcon((BufferedImage) value, 40, 40));
				} else {
					lb.setIcon(null);
				}
				
				return lb;
			}
		};
		
		cellRenderer.setHorizontalAlignment(0);
		
		for (int i = 0; i < table.getColumnCount(); i++) {
			var col = table.getColumn(table.getColumnName(i));
					
			col.setPreferredWidth(widthList[i]);
			col.setCellRenderer(cellRenderer);
		}
		
		center.add(createComp(new JScrollPane(table), 10, 110, 750, 380));
		updateList(true);
		
			
		tfList[1].addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				setVisible(false);
				
				var skillsFrame = new SelectSkillsFrame(strToNo(tfList[1].getText()));
				
				skillsFrame.btnSelect.addActionListener(e2 -> {
					
					if (skillsFrame.getCategoryStr().length() == 0)
						eMsg("������ �����ϼ���.");
					else
						skillsFrame.dispose();
						tfList[1].setText(skillsFrame.getCategoryStr());
				});
				
				skillsFrame.setVisible(true);
			};
		});
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = table.getSelectedRow();
				
				if (row < 0) return;
				
				setVisible(false);
				btnSubmit.setEnabled(false);
				
				int empNo = (Integer)dtm.getValueAt(row, 0);
				var frame = new CheckApplicantFrame(empNo);
				
				frame.addPrevForm(() -> setVisible(true));
				frame.btnCheck.addActionListener(e2 -> {
					frame.dispose();
					
					String eGender = dtm.getValueAt(row, 8).toString();
					String eGraduate = dtm.getValueAt(row, 7).toString();
					
					// ���� Ʋ��
					if (!eGender.equals("����") && 
							!eGender.equals(gender[Session.userGender])) {
						eMsg("������ �Ұ��մϴ�. (����)");
						return;
					}
					
					// �з� �̴�
					if (!eGraduate.equals("����") && 
							Session.userGraduate > Arrays.asList(grad).indexOf(eGraduate)) {
						eMsg("������ �Ұ��մϴ�. (�з�)");
						return;
					}
					
					var rs = getPreparedResultSet("SELECT * FROM applicant WHERE u_no = ? AND e_no = ? AND a_apply != 2", Session.userNo, empNo);
					
					try {
						// ���� ���� ��
						if (rs.next()) {
							eMsg("�հ��� �Ǵ� �ɻ����Դϴ�.");
							return;
						}
						
						iMsg("���� ������ �����Դϴ�.");
						btnSubmit.setEnabled(true);
					} catch (SQLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					
				});
				
				frame.setVisible(true);
			}
		});
	}
	
	void updateList(boolean allMode)
	{
		// ������ ����
		dtm.setRowCount(0);
		
		var rs = getPreparedResultSet("SELECT e.*, c.*, (SELECT COUNT(1) FROM applicant a WHERE a.e_no = e.e_no AND a.a_apply < 2) AS cnt\r\n"
				+ "FROM employment e\r\n"
				+ "INNER JOIN company c ON e.c_no = c.c_no "
				+ "HAVING cnt < e_people");
		
		try {
			while (rs.next()) {
				String graduate = grad[rs.getInt("e_graduate")];
				String eGender = gender[rs.getInt("e_gender")];
				String skills = noToStr(rs.getString("c_category"));
				
				// ��ü ��尡 �ƴ� �� ���͸� ����
				if (allMode == false) {
					// ����� ���� ����
					if (rs.getString("e_title").indexOf(tfList[0].getText()) == -1) continue;
					
					// ���� Ȯ��
					if (tfList[1].getText().length() > 0 && 
							Arrays.stream(skills.split(",")).allMatch(x -> tfList[1].getText().indexOf(x) == -1))
						continue;
					
					// ����
					if (cbList[0].getSelectedIndex() > 0 && 
							rs.getString("c_address").indexOf(cbList[0].getSelectedItem().toString()) == -1)
						continue;
					
					// �з�
					if (cbList[1].getSelectedIndex() > 0 && 
							graduate.indexOf(cbList[1].getSelectedItem().toString()) == -1)
						continue;
					
					// ����
					if (cbList[2].getSelectedIndex() > 0 && 
							eGender.indexOf(cbList[2].getSelectedItem().toString()) == -1)
						continue;
				}
				
				dtm.addRow(new Object[] {
					rs.getInt("e_no"),
					ImageIO.read(rs.getBlob("c_img").getBinaryStream()),
					rs.getString("e_title"),
					String.format("%d/%d", rs.getInt("cnt"), rs.getInt("e_people")),
					String.format("%,d", rs.getInt("e_pay")),
					skills,
					rs.getString("c_address"),
					graduate,
					eGender
				});
			}
			
			if (dtm.getRowCount() == 0) {
				eMsg("�˻� ����� �����ϴ�.");
				
				// allMode false�� ���� ��ü ��� ������Ʈ -> ���� ���� ����
				if (allMode == false) updateList(true);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static void main(String[] args) {
		Session.userNo = 1;
		Session.userName = "�̼�";
		Session.userGender = 2;
		Session.userGraduate = 0;
		Session.userImg = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
		
		new JobInfoFrame().setVisible(true);
	}

}
