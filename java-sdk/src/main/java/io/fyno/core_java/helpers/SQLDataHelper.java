package io.fyno.core_java.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import io.fyno.core_java.utils.Logger;

public class SQLDataHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "fyno.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLENAME_config = "config";
    private static final String config_Id = "id";
    private static final String config_Key = "key_name";
    private static final String config_Value = "value";
    private static final String TAG = "database_log";

    private SQLiteDatabase db;

    public SQLDataHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        db = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE " + TABLENAME_config +
                " (" + config_Id + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                config_Key + " TEXT," +
                config_Value + " TEXT);";
        try {
            db.execSQL(query);
        } catch (Exception e) {
            Logger.d("db", "onCreate - " + e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLENAME_config);
        } catch (Exception e) {
            Logger.d("db", "onUpgrade - " + e);
        }
        onCreate(db);
    }

    private void insertConfig(Config table_model_obj) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(config_Key, table_model_obj.key);
        contentValues.put(config_Value, table_model_obj.value);
        try {
            db.insert(TABLENAME_config, null, contentValues);
        } catch (Exception e) {
            Logger.d("db", "insert_config - " + e);
        }
    }

    public void insertConfigByKey(Config table_model_obj) {
        Cursor c = null;
        try {
            String q1 = "SELECT * FROM " + TABLENAME_config +
                    " WHERE " + config_Key + " = '" + table_model_obj.key + "'";
            c = db.rawQuery(q1, null);
            if (c != null && c.getCount() > 0) {
                if (c.moveToFirst()) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(config_Value, table_model_obj.value);
                    try {
                        db.update(TABLENAME_config,
                                contentValues,
                                config_Key + " = ? ",
                                new String[]{table_model_obj.key});
                    } catch (Exception e) {
                        Logger.d("db", "insert_configByKey - " + e);
                    }
                }
            } else {
                insertConfig(table_model_obj);
            }
        } catch (Exception e) {
            Logger.d("db", "insert_configByKey - " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public Config getConfigByKey(String key) {
        Config log = new Config(key);
        Cursor c = null;
        try {
            String query = "SELECT * FROM " + TABLENAME_config + " WHERE " + config_Key + " = '" + key + "'";
            c = db.rawQuery(query, null);
            if (c != null && c.moveToNext()) {
                do {
                    log.id = c.getInt(c.getColumnIndexOrThrow(config_Id));
                    log.key = c.getString(c.getColumnIndexOrThrow(config_Key));
                    log.value = c.getString(c.getColumnIndexOrThrow(config_Value));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            Logger.d("db", "getconfigByKey - " + e);
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return log;
    }

    public void deleteAllConfigs() {
        try {
            db.execSQL("DELETE FROM " + TABLENAME_config);
        } catch (Exception e) {
            Logger.d("db", "deleteAllConfigs - " + e);
        }
    }
}
