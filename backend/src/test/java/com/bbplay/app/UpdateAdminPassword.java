package com.bbplay.app;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class UpdateAdminPassword {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://192.168.12.22:5432/bbplay";
        String username = "bbplay";
        String password = "bbplay";
        String newPasswordHash = "$2a$10$5ozn8iCz9Bm83OjeEWiseOU94J3iW7yngy9yI0IXsi.F8VjHKud4K";
        
        try (Connection conn = DriverManager.getConnection(url, username, password)) {
            String sql = "UPDATE sys_user SET password = ? WHERE username = 'admin'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newPasswordHash);
                int rows = stmt.executeUpdate();
                System.out.println("更新成功，影响行数: " + rows);
                System.out.println("管理员密码已更新为: admin123");
            }
        } catch (Exception e) {
            System.err.println("更新失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
