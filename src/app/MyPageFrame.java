package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageFilter;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
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
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
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

public class MyPageFrame extends BaseFrame {
	
	DefaultTableModel dtm = new DefaultTableModel("no,번호,기업명,모집정보,시급,모집정원,최종학력,성별,합격여부".split(","), 0) {
		@Override
		public boolean isCellEditable(int row, int column) {
			// 셀 수정 막기
			return false;
		}
	};
	
	public MyPageFrame() {
		super("Mypage", 700, 400);
		
		addPrevForm(() -> new MainFrame().setVisible(true));
		
		setBorderLayout();
		north.add(createLabel(new JLabel("Mypage"), new Font("HY헤드라인M", 1, 24)));
		north.setBorder(new EmptyBorder(10, 10, 10, 10));
		center.setLayout(null);
		
		center.add(
				createComp(
						createLabel(new JLabel("성명 : " + Session.userName), new Font("굴림", 1, 18)),
						15, 0, 200, 30));
		
		center.add(
				createComp(
						createLabel(new JLabel("성별 : " + gender[Session.userGender]), new Font("굴림", 1, 16)),
						15, 30, 200, 30));
		center.add(
				createComp(
						createLabel(new JLabel("최종학력 : " + grad[Session.userGraduate]), new Font("굴림", 1, 16)),
						15, 60, 200, 30));
		
		
		var table = new JTable(dtm);
		var cellRenderer = new DefaultTableCellRenderer();
		
		cellRenderer.setHorizontalAlignment(0);
		
		// 열 사이즈 조정 막기
		// table.getTableHeader().setResizingAllowed(false);
		// 열 위치 변경 막기
		table.getTableHeader().setReorderingAllowed(false);
		
		// 번호 컬럼 숨김
		table.removeColumn(table.getColumn("no"));
		int[] widthList = {40, 100, 250, 80, 80, 120, 60, 60};
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		for (int i = 0; i < table.getColumnCount(); i++) {
			var col = table.getColumn(table.getColumnName(i));
			
			col.setCellRenderer(cellRenderer);
			col.setPreferredWidth(widthList[i]);
		}
		
		var popupMenu = new JPopupMenu();
		var menuItem = new JMenuItem("삭제");
		
		menuItem.addActionListener(e -> {
			int no = (Integer) dtm.getValueAt(table.getSelectedRow(), 0);
			
			executeSQL("DELETE FROM applicant WHERE a_no = " + no);
			
			iMsg("삭제가 완료되었습니다.");
			updateTable();
		});
		
		popupMenu.add(menuItem);
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				
				// 우측 클릭
				if (e.getButton() == MouseEvent.BUTTON3) {
					int row = table.rowAtPoint(new Point(e.getX(), e.getY()));
					
					table.addRowSelectionInterval(row, row);
					popupMenu.show(table, e.getX(), e.getY());
				}
				
			}
		});
		
		center.add(createComp(new JScrollPane(table), 15, 90, 650, 180));
		south.setLayout(new FlowLayout(FlowLayout.RIGHT));
		south.add(createButton("PDF 인쇄", e -> {
			try {
				table.print();
			} catch (PrinterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}));
		
		updateTable();
	}
	
	void updateTable() {
		
		dtm.setRowCount(0);
		
		// 테이블 데이터 추가
		var rs = getPreparedResultSet("SELECT a_no, c.c_name, e.e_title, e.e_pay, e.e_people, e.e_graduate, e.e_gender, a.a_apply\r\n"
				+ "FROM 2022지방_2.applicant a\r\n"
				+ "INNER JOIN employment e ON a.e_no = e.e_no\r\n"
				+ "INNER JOIN company c ON e.c_no = c.c_no\r\n"
				+ "WHERE a.u_no = " + Session.userNo);
		
		try {
			int no = 0;
			while (rs.next()) {
				dtm.addRow(new Object[] {
					rs.getInt("a_no"),
					++no,
					rs.getString("c_name"),
					rs.getString("e_title"),
					String.format("%,d", rs.getInt("e_pay")),
					rs.getInt("e_people"),
					grad[rs.getInt("e_graduate")],
					gender[rs.getInt("e_gender")],
					apply[rs.getInt("a_apply")]
				});
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		Session.userNo = 1;
		Session.userName = "이솜";
		Session.userGender = 2;
		Session.userGraduate = 2;
		
		new MyPageFrame().setVisible(true);
	}

}
