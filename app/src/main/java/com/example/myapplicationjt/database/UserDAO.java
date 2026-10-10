package com.example.myapplicationjt.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.myapplicationjt.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final DatabaseHelper dbHelper;

    public UserDAO(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean insertUser(User user) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_USER_ID, user.getUserId());
        cv.put(DatabaseHelper.COL_USER_NAME, user.getName());
        cv.put(DatabaseHelper.COL_USER_EMAIL, user.getEmail().toLowerCase());
        cv.put(DatabaseHelper.COL_USER_PASSWORD, user.getPassword());
        String address = (user.getAddress() != null && !user.getAddress().trim().isEmpty())
                ? user.getAddress().trim()
                : "ยังไม่ได้ระบุที่อยู่";
        cv.put(DatabaseHelper.COL_USER_ADDRESS, address);

        long result = db.insertWithOnConflict(DatabaseHelper.TABLE_USERS, null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        return result != -1;
    }

    public User login(String email, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                null,
                DatabaseHelper.COL_USER_EMAIL + "=? AND " + DatabaseHelper.COL_USER_PASSWORD + "=?",
                new String[]{email.toLowerCase().trim(), password.trim()},
                null, null, null
        );

        User user = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                String mail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL));
                String pass = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ADDRESS));

                user = new User(id, name, mail, pass);
                user.setAddress(address);
            }
            cursor.close();
        }
        return user;
    }

    public boolean updateAddress(String email, String address) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.COL_USER_ADDRESS, address);

        int rows = db.update(
                DatabaseHelper.TABLE_USERS,
                cv,
                DatabaseHelper.COL_USER_EMAIL + "=?",
                new String[]{email.toLowerCase().trim()}
        );
        return rows > 0;
    }

    public boolean updateUserProfile(String email, String name, String password) {
        if (email == null || email.trim().isEmpty()) return false;
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        if (name != null && !name.trim().isEmpty()) {
            cv.put(DatabaseHelper.COL_USER_NAME, name.trim());
        }
        if (password != null && !password.trim().isEmpty()) {
            cv.put(DatabaseHelper.COL_USER_PASSWORD, password.trim());
        }

        if (cv.size() == 0) return false;

        int rows = db.update(
                DatabaseHelper.TABLE_USERS,
                cv,
                DatabaseHelper.COL_USER_EMAIL + "=?",
                new String[]{email.toLowerCase().trim()}
        );
        return rows > 0;
    }

    public String getAddress(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COL_USER_ADDRESS},
                DatabaseHelper.COL_USER_EMAIL + "=?",
                new String[]{email.toLowerCase().trim()},
                null, null, null
        );

        String address = "";
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                address = cursor.getString(0);
            }
            cursor.close();
        }
        return address != null ? address : "";
    }

    public String getUserName(String email) {
        if (email == null || email.trim().isEmpty()) return "";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COL_USER_NAME},
                DatabaseHelper.COL_USER_EMAIL + "=?",
                new String[]{email.toLowerCase().trim()},
                null, null, null
        );

        String name = "";
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                name = cursor.getString(0);
            }
            cursor.close();
        }
        return name != null ? name : "";
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null, null, null, null, null, DatabaseHelper.COL_USER_NAME + " ASC");

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_NAME));
                String mail = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL));
                String pass = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ADDRESS));

                User u = new User(id, name, mail, pass);
                u.setAddress(address);
                list.add(u);
            }
            cursor.close();
        }
        return list;
    }

    public void clearAll() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_USERS, null, null);
    }
}
