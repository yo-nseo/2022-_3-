package db;

import java.io.FileInputStream;
import java.sql.DriverManager;

import javax.swing.JOptionPane;

import app.BaseFrame;

public class Setting {

	public static void main(String[] args) {

		try {
			var con = DriverManager.getConnection(
					"jdbc:mysql://localhost/?serverTimezone=UTC&allowLoadLocalInfile=true", "root", "1234");
			var stmt = con.createStatement();

			// LOAD DATA 보안 옵션 설정
			stmt.execute("SET GLOBAL local_infile= 1");

			// DB 존재시 제거
			stmt.execute("DROP SCHEMA IF EXISTS `2022지방_2`");
			// DB 생성
			stmt.execute("CREATE SCHEMA `2022지방_2` DEFAULT CHARACTER SET utf8 ;");

			// 테이블 생성
			stmt.execute("CREATE TABLE `2022지방_2`.`company` (\r\n"
					+ "  `c_no` INT NOT NULL AUTO_INCREMENT,\r\n"
					+ "  `c_name` VARCHAR(10) NULL,\r\n" 
					+ "  `c_ceo` VARCHAR(10) NULL,\r\n"
					+ "  `c_address` VARCHAR(100) NULL,\r\n" 
					+ "  `c_category` VARCHAR(15) NULL,\r\n"
					+ "  `c_employee` INT NULL,\r\n" 
					+ "  `c_img` LONGBLOB NULL,\r\n" 
					+ "  `c_search` INT NULL,\r\n"
					+ "  PRIMARY KEY (`c_no`));");

			stmt.execute("CREATE TABLE `2022지방_2`.`user` (\r\n" 
					+ "  `u_no` INT NOT NULL AUTO_INCREMENT,\r\n"
					+ "  `u_name` VARCHAR(10) NULL,\r\n" 
					+ "  `u_id` VARCHAR(10) NULL,\r\n"
					+ "  `u_pw` VARCHAR(15) NULL,\r\n" 
					+ "  `u_birth` VARCHAR(15) NULL,\r\n"
					+ "  `u_email` VARCHAR(30) NULL,\r\n" 
					+ "  `u_gender` INT NULL,\r\n"
					+ "  `u_graduate` INT NULL,\r\n" 
					+ "  `u_address` VARCHAR(100) NULL,\r\n"
					+ "  `u_img` LONGBLOB NULL,\r\n" 
					+ "  PRIMARY KEY (`u_no`));\r\n" 
					+ "");

			stmt.execute("CREATE TABLE `2022지방_2`.`employment` (\r\n"
					+ "  `e_no` INT NOT NULL AUTO_INCREMENT,\r\n"
					+ "  `c_no` INT NULL,\r\n" 
					+ "  `e_title` VARCHAR(30) NULL,\r\n" 
					+ "  `e_pay` INT NULL,\r\n"
					+ "  `e_people` INT NULL,\r\n" 
					+ "  `e_gender` INT NULL,\r\n" 
					+ "  `e_graduate` INT NULL,\r\n"
					+ "  PRIMARY KEY (`e_no`),\r\n" 
					+ "  INDEX `FK_employment_c_no_idx` (`c_no` ASC) VISIBLE,\r\n"
					+ "  CONSTRAINT `FK_employment_c_no`\r\n" 
					+ "    FOREIGN KEY (`c_no`)\r\n"
					+ "    REFERENCES `2022지방_2`.`company` (`c_no`)\r\n" 
					+ "    ON DELETE CASCADE\r\n"
					+ "    ON UPDATE CASCADE);\r\n" + "");

			stmt.execute("CREATE TABLE `2022지방_2`.`applicant` (\r\n" 
					+ "  `a_no` INT NOT NULL AUTO_INCREMENT,\r\n"
					+ "  `e_no` INT NULL,\r\n" 
					+ "  `u_no` INT NULL,\r\n" 
					+ "  `a_apply` INT NULL,\r\n"
					+ "  PRIMARY KEY (`a_no`),\r\n" 
					+ "  INDEX `FK_appliant_u_no_idx` (`u_no` ASC) VISIBLE,\r\n"
					+ "  INDEX `FK_appliant_e_no_idx` (`e_no` ASC) VISIBLE,\r\n" 
					+ "  CONSTRAINT `FK_appliant_e_no`\r\n"
					+ "    FOREIGN KEY (`e_no`)\r\n" 
					+ "    REFERENCES `2022지방_2`.`employment` (`e_no`)\r\n"
					+ "    ON DELETE CASCADE\r\n" 
					+ "    ON UPDATE CASCADE,\r\n" 
					+ "  CONSTRAINT `FK_appliant_u_no`\r\n"
					+ "    FOREIGN KEY (`u_no`)\r\n" 
					+ "    REFERENCES `2022지방_2`.`user` (`u_no`)\r\n"
					+ "    ON DELETE CASCADE\r\n" 
					+ "    ON UPDATE CASCADE);\r\n" + "");

			stmt.execute("USE 2022지방_2");

			var tables = "user,company,employment,applicant".split(",");

			for (String table : tables) {
				stmt.execute(
						"LOAD DATA LOCAL INFILE 'datafiles/" + table + ".txt' INTO TABLE " + table + " IGNORE 1 LINES");
			}

			// CRUD U update
			var con2 = DriverManager.getConnection(
					"jdbc:mysql://localhost/2022지방_2?serverTimezone=UTC&allowLoadLocalInfile=true", "root", "1234");
			var pstmtCompany = con2.prepareStatement("UPDATE company SET c_img = ? WHERE c_no = ?");
			var pstmtUser = con2.prepareStatement("UPDATE user SET u_img = ? WHERE u_no = ?");

			try (var rs = stmt.executeQuery("SELECT * FROM company")) {
				while (rs.next()) {
					pstmtCompany.setBlob(1, new FileInputStream("datafiles/기업/" + rs.getString("c_name") + "1.jpg"));
					pstmtCompany.setInt(2, rs.getInt("c_no"));
					pstmtCompany.executeUpdate();
				}
			}

			try (var rs = stmt.executeQuery("SELECT * FROM user")) {
				while (rs.next()) {
					pstmtUser.setBlob(1, new FileInputStream("datafiles/회원사진/" + rs.getString("u_no") + ".jpg"));
					pstmtUser.setInt(2, rs.getInt("u_no"));
					pstmtUser.executeUpdate();
				}
			}

			stmt.execute("DROP USER IF EXISTS 'user'@'localhost'");
			stmt.execute("CREATE USER 'user'@'localhost' IDENTIFIED BY '1234'");
			stmt.execute("GRANT SELECT, INSERT, UPDATE, DELETE ON 2022지방_2.* TO 'user'@'localhost'");

			BaseFrame.iMsg("셋팅 성공");

		} catch (Exception e) {
			e.printStackTrace();
			BaseFrame.eMsg("셋팅 실패");
		}

	}

}
