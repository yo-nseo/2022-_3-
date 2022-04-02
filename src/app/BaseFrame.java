package app;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class BaseFrame extends JFrame {
	// static -> 바뀌지 않으니까 (미리 선언) 
	static final String[] categoryList = "편의점,영화관,화장품,음식점,백화점,의류점,커피전문점,은행".split(",");
	// 1번부터 시작해서 ,를 앞에 붙임
	static final String[] gender = ",남자,여자,무관".split(",");
	static final String[] grad = "대학교 졸업,고등학교 졸업,중학교 졸업,무관".split(",");
	static final String[] apply = "심사중,합격,불합격".split(",");
	
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	static Connection con;
	static Statement stmt;
	
	// 콤보박스에서 한번에 보여줄려고 -> adminchartframe에서 
	static class ComboItem {
		int id;
		String text;
		
		public ComboItem(int id, String text) {
			this.id = id;
			this.text = text;
		}
		
		// 이거 안쓰면 이상하게 보임
		@Override
		public String toString() {
			return text;
		}
	}
	
	// panel 동, 서, 남, 북, 센터
	// 각각 가지고 있어야한다. -> static이 안들어간 객체
	JPanel north = new JPanel();
	JPanel south = new JPanel();
	JPanel center = new JPanel();
	JPanel east = new JPanel();
	JPanel west = new JPanel();
	Thread thread;
	WindowListener prevListener;
	
	// staic 생성자 -> staitc 쵝화 해주는것 
	static {
		try {
			con = DriverManager.getConnection("jdbc:mysql://localhost/2022지방_2?serverTimezone=UTC", "root", "1234");
			stmt = con.createStatement();
			sdf.setLenient(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// 들어온 값을 이용해 화면 창 만들기 -> 기본
	public BaseFrame(String title, int width, int height) {
		setTitle(title);
		setSize(width, height);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				destroyThread();
			}
		});
	}
	
	// 폼이 종료 될때마다 실행하는 기능들 -> 뒤로 돌아가는 기능들
	BaseFrame addPrevForm(Runnable r) {
		prevListener = new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				r.run();
			}
		};
		
		addWindowListener(prevListener);
		
		return this;
	}
	
	// 뒤로 돌아가지 않고 폼을 끄라 -> 로그인 할때 
	void disposewithRemovingPrevForm() {
		removeWindowListener(prevListener);
		dispose();
	}
	
	// 스레드가 있으면 죽여라 -> 새로운 애니메이션 만들때, 폼이 닫힐때
	protected void destroyThread() {
		if (thread != null && thread.isAlive())
			thread.interrupt();
	}
	
	// -> insert, update 전용
	public static int executeSQL(String sql, Object ...objects) {
		
		try {
			// insert쿼리 할때만 사용
			var pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			
			for (int i = 0; i < objects.length; i++) {
				pstmt.setObject(i + 1, objects[i]);
			}
			
			pstmt.executeUpdate();
			
			var rs = pstmt.getGeneratedKeys();
			
			if (rs.next()) {
				return rs.getInt(1);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return -1;
	}
	
	// select 쿼리 쓸때 -> 읽기
	public static ResultSet getPreparedResultSet(String sql, Object ...objects) {	
		try {
			var pstmt = con.prepareStatement(sql);
			
			for (int i = 0; i < objects.length; i++) {
				pstmt.setObject(i + 1, objects[i]);
			}
			
			return pstmt.executeQuery();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void setBorderLayout()
	{
		setLayout(new BorderLayout());
		add(north, BorderLayout.NORTH);
		add(south, BorderLayout.SOUTH);
		add(west, BorderLayout.WEST);
		add(east, BorderLayout.EAST);
		add(center);
	}
	
	public static JLabel createLabel(JLabel lb, Font font) {
		lb.setFont(font);
		
		return lb;
	}
	
	// 이미지 레이블 만드는것
	public static JLabel createLabel(BufferedImage img, int w, int h) {
		var lb = new JLabel();
		
		lb.setIcon(getResizedIcon(img, w, h));
		lb.setBorder(new LineBorder(Color.BLACK));
		
		return lb;
	}
	
	public static ImageIcon getResizedIcon(BufferedImage img, int w, int h) {
		return new ImageIcon(img.getScaledInstance(w, h, BufferedImage.SCALE_SMOOTH));
	}
	
	public static <T extends JComponent> T createComp(T comp, int x, int y, int w, int h) {
		comp.setPreferredSize(new Dimension(w, h));
		comp.setBounds(x, y, w, h);
		
		return comp;
	}
	
	public static <T extends JComponent> T createComp(T comp, int w, int h) {
		return createComp(comp, 0, 0, w, h);
	}
	
	// 버튼 만들 때 actionlistener 도 같이 만드는 것
	public static JButton createButton(String text, ActionListener action) {
		var btn = new JButton(text);

		btn.addActionListener(action);
		
		return btn;
	}
	
	public static void iMsg(String msg) {
		JOptionPane.showMessageDialog(null, msg, "정보", JOptionPane.INFORMATION_MESSAGE | JOptionPane.OK_OPTION);
	}
	
	public static void eMsg(String msg) {
		JOptionPane.showMessageDialog(null, msg, "경고", JOptionPane.ERROR_MESSAGE | JOptionPane.OK_OPTION);
	}
	
	public static String strToNo(String category) {
		var tmp = new ArrayList<String>();
		
		if (category.length() == 0) return "";
		
		for (String s : category.split(",")) {
			tmp.add("" + (Arrays.asList(categoryList).indexOf(s) + 1));
		}
		
		return String.join(",", tmp);
	}
	
	public static String noToStr(String category) {
		var tmp = new ArrayList<String>();
		
		for (String s : category.split(",")) {
			tmp.add(categoryList[Integer.valueOf(s) - 1]);
		}
		
		return String.join(",", tmp);
	}

}
