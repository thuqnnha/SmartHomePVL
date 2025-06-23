package com.example.smarthomepvl;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.smarthomepvl.Device;


public class DatabaseHelper {
    private static String URL = "jdbc:mysql://pvl.vn:3306/admin_db";
    private static String USER = "raspberry";
    private static String PASSWORD = "admin6789@";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final Context context;

    public DatabaseHelper(Context context) {
        this.context = context;
    }
    public static void updateConnectionInfo(String dbName, String user, String pass) {
        URL = "jdbc:mysql://pvl.vn:3306/" + dbName + "?useSSL=false&serverTimezone=UTC";
        USER = user;
        PASSWORD = pass;
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
    public void checkLoginDB(String dbName, String username, String password, LoginCallback callback) {
        executorService.execute(() -> {
            String dynamicUrl = "jdbc:mysql://pvl.vn:3306/" + dbName + "?useSSL=false&serverTimezone=UTC";

            try (Connection testConn = DriverManager.getConnection(dynamicUrl, username, password)) {
                // Nếu kết nối thành công, cập nhật biến static
                updateConnectionInfo(dbName, username, password);

                ((Activity) context).runOnUiThread(() -> {
                    callback.onResult(true);
                });
            } catch (Exception e) {
                e.printStackTrace();
                ((Activity) context).runOnUiThread(() -> {
                    callback.onResult(false);
                });
            }
        });
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
            String query = "SELECT IDPhong, TenPhong FROM smarthome_rooms";
            List<Room> roomList = new ArrayList<>();

            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(query);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("IDPhong");
                    String name = rs.getString("TenPhong");
                    int iconResId = getIconForRoom(name);
                    roomList.add(new Room(id,name, iconResId, R.drawable.room_item_background));
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
    public static boolean updateRoom(String name, int IDPhong) {
        String query = "UPDATE smarthome_rooms SET TenPhong = ? WHERE IDPhong = ?";
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, name);
            ps.setInt(2, IDPhong);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean deleteRoom(int IDPhong) {
        String query = "DELETE FROM smarthome_rooms WHERE IDPhong = ?";
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, IDPhong);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public void loadDeviceInRoom(int idPhong, RoomDetailFragment.DeviceCallback callback) {
        executorService.execute(() -> {
            String query = "SELECT ID, DiaChiMAC, TenThietBi, LoaiThietBi FROM smarthome_device WHERE IDPhong = ?";
            List<Device> deviceList = new ArrayList<>();

            try (Connection conn = connect();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setInt(1, idPhong);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt("ID");
                    String mac = rs.getString("DiaChiMAC");
                    String ten = rs.getString("TenThietBi");
                    int loai = rs.getInt("LoaiThietBi");

                    deviceList.add(new Device(id, mac, ten,loai));
                }

                new Handler(Looper.getMainLooper()).post(() -> callback.onDevicesLoaded(deviceList));

            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Lỗi khi tải thiết bị"));
            }
        });
    }

//    public void loadCameraList(CameraFragment.DeviceCallback callback) {
//        executorService.execute(() -> {
//            String query = "SELECT ID, DiaChiMAC, TenThietBi, LoaiThietBi FROM smarthome_device WHERE LoaiThietBi = 0";
//            List<Device> cameraList = new ArrayList<>();
//
//            try (Connection conn = connect();
//                 PreparedStatement ps = conn.prepareStatement(query)) {
//
//                ResultSet rs = ps.executeQuery();
//
//                while (rs.next()) {
//                    int id = rs.getInt("ID");
//                    String mac = rs.getString("DiaChiMAC");
//                    String ten = rs.getString("TenThietBi");
//                    int loai = rs.getInt("LoaiThietBi");
//
//                    cameraList.add(new Device(id, mac, ten, loai));
//                }
//
//                new Handler(Looper.getMainLooper()).post(() -> callback.onDevicesLoaded(cameraList));
//
//            } catch (Exception e) {
//                e.printStackTrace();
//                new Handler(Looper.getMainLooper()).post(() -> callback.onError("Lỗi khi tải danh sách camera"));
//            }
//        });
//    }
    public static boolean insertCamera(String DiaChiMAC, String TenThietBi, int IDPhong) {
        String query = "INSERT INTO smarthome_device (DiaChiMAC, TenThietBi, LoaiThietBi, IDPhong) VALUES (?,?,?,?)";
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, DiaChiMAC);
            ps.setString(2, TenThietBi);
            ps.setInt(3, 0);
            ps.setInt(4, IDPhong);


            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean updateCamera(String DiaChiMAC, String TenThietBi, int ID) {
        String query = "UPDATE smarthome_device SET DiaChiMAC = ?, TenThietBi = ? WHERE ID = ?";
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, DiaChiMAC);
            ps.setString(2, TenThietBi);
            ps.setInt(3, ID);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean DeleteCamera(int ID) {
        String query = "DELETE FROM smarthome_device WHERE ID = ?";
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setInt(1, ID);

            int rowsUpdated = ps.executeUpdate();
            return rowsUpdated > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean insertDevice(String DiaChiMAC, String TenThietBi, int IDPhong) {
        String query = "INSERT INTO smarthome_device (DiaChiMAC, TenThietBi, LoaiThietBi, IDPhong) VALUES (?,?,?,?)";
        try (Connection connection = connect();
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, DiaChiMAC);
            ps.setString(2, TenThietBi);
            ps.setInt(3, 1);
            ps.setInt(4, IDPhong);


            int rowsInserted = ps.executeUpdate();
            return rowsInserted > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public static List<ChartEntry> loadChartData(String MACID, Date startTime) {
        List<ChartEntry> chartList = new ArrayList<>();
        String query = "SELECT DateTime, TimeOn, TimeOff, Status, Current, Voltage, Temperature FROM " + MACID + " WHERE DateTime >= ? ORDER BY DateTime ASC";
        Log.d("ChartEntryDebug", query);
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setTimestamp(1, new java.sql.Timestamp(startTime.getTime()));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String timeOn = rs.getTime("TimeOn").toString();   // Định dạng sẵn là HH:mm:ss
                String timeOff = rs.getTime("TimeOff").toString();

                ChartEntry entry = new ChartEntry(
                        rs.getTimestamp("DateTime"),      // Dạng java.util.Date
                        timeOn,
                        timeOff,
                        rs.getString("Status"),
                        rs.getFloat("Current"),
                        rs.getFloat("Voltage"),
                        rs.getFloat("Temperature")
                );

                chartList.add(entry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chartList;
    }
    public static List<ChartEntry> getStatusDevice(String MACID) {
        List<ChartEntry> chartList = new ArrayList<>();
        String query = "SELECT DateTime, TimeOn, TimeOff, Status, Current, Voltage, Temperature FROM " + MACID + " ORDER BY ID DESC LIMIT 1";
        Log.d("ChartEntryDebug", query);
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String timeOn = rs.getTime("TimeOn").toString();   // Định dạng sẵn là HH:mm:ss
                String timeOff = rs.getTime("TimeOff").toString();

                ChartEntry entry = new ChartEntry(
                        rs.getTimestamp("DateTime"),      // Dạng java.util.Date
                        timeOn,
                        timeOff,
                        rs.getString("Status"),
                        rs.getFloat("Current"),
                        rs.getFloat("Voltage"),
                        rs.getFloat("Temperature")
                );

                chartList.add(entry);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return chartList;
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