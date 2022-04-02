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

public class AdminJobInfoFrame extends BaseFrame {
	DefaultTableModel dtm = new DefaultTableModel("no,이미지,공고명,모집정원,시급,직종,지역,학력,성별".split(","), 0) {
		public boolean isCellEditable(int row, int column) {
			return false;
		};
	};
	
	public AdminJobInfoFrame() {
		super("관리자 채용정보", 800, 600);
		
		addPrevForm(() -> new AdminMainFrame().setVisible(true));
		setBorderLayout();
		north.setLayout(new FlowLayout(FlowLayout.LEFT));
		north.add(createLabel(createComp(new JLabel("관리자 채용정보", 0), 650, 30), new Font("HY헤드라인M", 1, 24)));
		
		north.setBorder(new EmptyBorder(10, 10, 10, 10));
		center.setLayout(null);
		
		var table = new JTable(dtm);
		
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
		
		center.add(createComp(new JScrollPane(table), 10, 10, 750, 470));
		updateList();
		
		north.add(createButton("공고수정", e -> {
			int row = table.getSelectedRow();
			
			if (row == -1) {
				eMsg("수정할 공고를 선택하세요.");
				return;
			}
			
			disposewithRemovingPrevForm();
			new AdminEmploymentFrame((Integer) dtm.getValueAt(row, 0))
				.addPrevForm(() -> new AdminJobInfoFrame().setVisible(true))
				.setVisible(true);
			
		}));
	}
	
	void updateList() {
		
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new AdminJobInfoFrame().setVisible(true);
	}

}
