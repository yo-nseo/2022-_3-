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
	DefaultTableModel dtm = new DefaultTableModel("no,이미지,공고명,모집정원,시급,직종,지역,학력,성별".split(","), 0) {
		public boolean isCellEditable(int row, int column) {
			return false;
		};
	};
	JTextField[] tfList = {
		new JTextField(14), new JTextField(14)
	};
	JComboBox[] cbList = {
		new JComboBox("전체,서울,부산,대구,인천,광주,대전,울산,세종,경기,강원,충북,충남,전북,전남,경북,경남,제주".split(",")),
		new JComboBox("전체,대학교 졸업,고등학교 졸업,중학교 졸업,무관".split(",")),
		new JComboBox("전체,남자,여자,무관".split(","))
	};
	
	
	public JobInfoFrame() {
		super("채용정보", 800, 600);
		
		setBorderLayout();
		addPrevForm(() -> new MainFrame().setVisible(true));
		north.add(createLabel(new JLabel("채용정보"), new Font("HY헤드라인M", 1, 24)));
		north.setBorder(new EmptyBorder(10, 10, 10, 10));
		center.setLayout(null);
		
		JPanel[] flow = {
				new JPanel(new FlowLayout(FlowLayout.LEFT) ),
				new JPanel(new FlowLayout(FlowLayout.LEFT) ),
				new JPanel(new FlowLayout(FlowLayout.LEFT) ),
				new JPanel(new FlowLayout(FlowLayout.LEFT) )
		};
		
		tfList[1].setEditable(false);
		
		flow[0].add(createComp(new JLabel("공고명", 0), 60, 24));
		flow[0].add(tfList[0]);
		flow[1].add(createComp(new JLabel("직종", 0), 60, 24));
		flow[1].add(tfList[1]);
		flow[2].add(createComp(new JLabel("지역", 0), 60, 24));
		flow[2].add(cbList[0]);
		flow[2].add(createComp(new JLabel("학력", 0), 60, 24));
		flow[2].add(cbList[1]);
		flow[2].add(createComp(new JLabel("성별", 0), 60, 24));
		flow[2].add(cbList[2]);
		
		var table = new JTable(dtm);
		var btnSubmit = createButton("지원하기", e -> {
			executeSQL("INSERT INTO applicant VALUES(0, ?, ?, 0)", 
					dtm.getValueAt(table.getSelectedRow(), 0),
					Session.userNo);
			
			iMsg("신청이 완료되었습니다.");
			
			updateList(false);
		});
		
		btnSubmit.setEnabled(false);
		
		flow[3].add(createButton("검색하기", e -> updateList(false)));
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
						eMsg("직종을 선택하세요.");
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
					
					// 성별 틀림
					if (!eGender.equals("무관") && 
							!eGender.equals(gender[Session.userGender])) {
						eMsg("지원이 불가합니다. (성별)");
						return;
					}
					
					// 학력 미달
					if (!eGraduate.equals("무관") && 
							Session.userGraduate > Arrays.asList(grad).indexOf(eGraduate)) {
						eMsg("지원이 불가합니다. (학력)");
						return;
					}
					
					var rs = getPreparedResultSet("SELECT * FROM applicant WHERE u_no = ? AND e_no = ? AND a_apply != 2", Session.userNo, empNo);
					
					try {
						// 현재 지원 중
						if (rs.next()) {
							eMsg("합격자 또는 심사중입니다.");
							return;
						}
						
						iMsg("지원 가능한 공고입니다.");
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
		// 데이터 리셋
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
				
				// 전체 모드가 아닐 시 필터링 동작
				if (allMode == false) {
					// 공고명 포함 여부
					if (rs.getString("e_title").indexOf(tfList[0].getText()) == -1) continue;
					
					// 직종 확인
					if (tfList[1].getText().length() > 0 && 
							Arrays.stream(skills.split(",")).allMatch(x -> tfList[1].getText().indexOf(x) == -1))
						continue;
					
					// 지역
					if (cbList[0].getSelectedIndex() > 0 && 
							rs.getString("c_address").indexOf(cbList[0].getSelectedItem().toString()) == -1)
						continue;
					
					// 학력
					if (cbList[1].getSelectedIndex() > 0 && 
							graduate.indexOf(cbList[1].getSelectedItem().toString()) == -1)
						continue;
					
					// 성별
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
				eMsg("검색 결과가 없습니다.");
				
				// allMode false일 때만 전체 모드 업데이트 -> 무한 루프 방지
				if (allMode == false) updateList(true);
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public static void main(String[] args) {
		Session.userNo = 1;
		Session.userName = "이솜";
		Session.userGender = 2;
		Session.userGraduate = 0;
		Session.userImg = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
		
		new JobInfoFrame().setVisible(true);
	}

}
