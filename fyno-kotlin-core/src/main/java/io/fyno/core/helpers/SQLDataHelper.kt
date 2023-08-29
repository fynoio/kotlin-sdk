package io.fyno.core.helpers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.fyno.core.utils.Logger

class Config(
    var key: String? = null,
    var value: String? = null,
    var id: Int?=null
)

class SQLDataHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    private val db: SQLiteDatabase = writableDatabase

    override fun onCreate(db: SQLiteDatabase) {
        val query =
            "CREATE TABLE " + TABLENAME_config + " (" + config_Id + " INTEGER PRIMARY KEY AUTOINCREMENT," + config_Key + " TEXT ," + config_Value + " TEXT " + ");"
        try {
            db.execSQL(query)
        } catch (e: Exception) {
            Logger.e("db", "onCreate", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            db.execSQL("DROP TABLE IF EXISTS " + TABLENAME_config)
        } catch (e: Exception) {
            Logger.e("db", "onUpgrade", e)
        }
        onCreate(db)
    }

    private fun insert_config(table_model_obj: Config) {
        val contentValues = ContentValues()
        contentValues.put(config_Key, table_model_obj.key)
        contentValues.put(config_Value, table_model_obj.value)
        try {
            db.insert(TABLENAME_config, null, contentValues)
        } catch (e: Exception) {
            Logger.e("db", "insert_config", e)
        }
    }

    fun insert_configByKey(table_model_obj: Config) {
        var c: Cursor? = null
        try {
            val q1 =
                " SELECT * FROM " + TABLENAME_config + " where " + config_Key + " ='" + table_model_obj.key + "'"
            c = db.rawQuery(q1, null)
            if (c != null && c.count > 0) {
                if (c.moveToFirst()) {
                    val contentValues = ContentValues()
                    contentValues.put(config_Value, table_model_obj.value)
                    try {
                        db.update(TABLENAME_config,
                            contentValues,
                            "$config_Key= ? ",
                            arrayOf(table_model_obj.key))
                    } catch (e: Exception) {
                        Logger.e("db", "insert_configByKey", e)
                    }
                }
            } else {
                insert_config(table_model_obj)
            }
            c.close()
        } catch (e: Exception) {
            Logger.e("db", "insert_configByKey", e)
        } finally {
            c?.close()
        }
    }


    fun getconfigByKey(key: String): Config {
        val log = Config()
        var c: Cursor? = null
        try {
            val query =
                "select * from " + TABLENAME_config + " where " + config_Key + " ='" + key + "'"
            c = db.rawQuery(query, null)
            if (c != null && c.moveToNext()) {
                do {
                    log.id = (c.getInt(c.getColumnIndexOrThrow(config_Id)))
                    log.key = (c.getString(c.getColumnIndexOrThrow(config_Key)))
                    log.value = (c.getString(c.getColumnIndexOrThrow(config_Value)))
                } while (c.moveToNext())
            }
            c.close()
        } catch (e: Exception) {
            Logger.e("db", "getconfigByKey", e)
        } finally {
            c?.close()
        }
        return log
    }

    fun deleteAllConfigs() {
        try {
            db.execSQL("DELETE FROM " + TABLENAME_config)
        } catch (e: Exception) {
            Logger.e("db", "deleteAllConfigs", e)
        }
    }

    companion object {
        private const val DATABASE_NAME = "fyno.db"
        private const val DATABASE_VERSION = 1
        private const val TABLENAME_config = "config"
        private const val config_Id = "id"
        private const val config_Key = "key_name"
        private const val config_Value = "value"
        private const val TAG = "database_log"
    }
}