package com.example.smarthomepvl;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseHelper {
    private static final String URL = "jdbc:mysql://pvl.vn:3306/admin_db";
    private static final String USER = "raspberry";
    private static final String PASSWORD = "admin6789@";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Context context;

    public DatabaseHelper(Context context) {
        this.context = context;
    }


    public void close() {
        executorService.shutdown();
    }
    // Kết nối đến cơ sở dữ liệu
    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }


    public static boolean insertUser(String username, String password, String email, String phone) {
        String query = "INSERT INTO smarthome_accounts (TaiKhoan, MatKhau, Email, SoDienThoai) VALUES (?, ?, ?, ?)";
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, phone);

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public interface LoginCallback {
        void onResult(boolean success);
    }

    public void checkLogin(String username, String password, LoginCallback callback) {
        executorService.execute(() -> {
            String query = "SELECT * FROM smarthome_accounts WHERE TaiKhoan = ? AND MatKhau = ?";
            try (Connection connection = connect();
                 PreparedStatement ps = connection.prepareStatement(query)) {

                ps.setString(1, username);
                ps.setString(2, password);

                ResultSet rs = ps.executeQuery();
                boolean success = rs.next(); // có dòng dữ liệu nghĩa là đúng

                // Gửi kết quả về UI thread
                ((android.app.Activity) context).runOnUiThread(() -> {
                    callback.onResult(success);
                });

            } catch (Exception e) {
                e.printStackTrace();
                ((android.app.Activity) context).runOnUiThread(() -> {
                    callback.onResult(false);
                });
            }
        });
    }
    public interface RoomCallback {
        void onRoomsLoaded(List<Room> rooms);
        void onError(String message);
    }

    public void loadRoom(RoomCallback callback) {
        executorService.execute(() -> {
            String query = "SELECT TenPhong FROM smarthome_rooms";
            List<Room> roomList = new ArrayList<>();

            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String name = rs.getString("TenPhong");
                    int iconResId = getIconForRoom(name);
                    roomList.add(new Room(name, iconResId, R.drawable.room_item_background));
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.onRoomsLoaded(roomList));

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Lỗi khi tải phòng"));
            }
        });
    }

    private int getIconForRoom(String name) {
        name = name.toLowerCase();
        if (name.contains("khách")) return R.drawable.ic_living_room;
        if (name.contains("ngủ")) return R.drawable.ic_bedroom;
        if (name.contains("bếp")) return R.drawable.ic_kitchen;
        return R.drawable.ic_room;
    }


    public static boolean insertRoom(String TenPhong) {
        String query = "INSERT INTO smarthome_rooms (TenPhong) VALUES (?)";
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, TenPhong);

            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateUser(String oldusername, String newusername, String password, String phone, String email) {
        String query = "INSERT INTO quanlykho_users (Username = ?, Password = ?, Phone = ?, Email = ? WHERE Username = ?)";
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, newusername);
            ps.setString(2, password);
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.setString(5, oldusername);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static boolean updateData(String maKho, String maVatTu, String tenVatTu, String donViTinh,
                                     String viTri, String soLuongNhap, String giaTien) {
        String query = "INSERT INTO quanlykho_data (MaKho, MaVatTu, TenVatTu, DonViTinh, ViTri, SoLuongNhap, ThoiGianNhap, GiaTien) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, maKho);
            ps.setString(2, maVatTu);
            ps.setString(3, tenVatTu);
            ps.setString(4, donViTinh);
            ps.setString(5, viTri);
            ps.setString(6, soLuongNhap);

            // Thời gian hiện tại
            ps.setTimestamp(7, new Timestamp(System.currentTimeMillis())); // Thời gian hiện tại
            ps.setString(8, giaTien);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateCount(int soLuongBan, String maVatTu) {
        String query = "UPDATE quanlykho_data SET SoLuongNhap = SoLuongNhap - ? WHERE MaVatTu = ? AND SoLuongNhap >= ?";

        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, soLuongBan);
            ps.setString(2, maVatTu);
            ps.setInt(3, soLuongBan);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }





}