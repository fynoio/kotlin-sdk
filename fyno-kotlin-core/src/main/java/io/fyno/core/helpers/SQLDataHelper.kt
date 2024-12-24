package io.fyno.core.helpers

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import io.fyno.core.RequestHandler
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
        Log.i(TAG, "onCreate: Creating Fyno databases");
        val createTableQuery = "CREATE TABLE IF NOT EXISTS api_responses (id INTEGER PRIMARY KEY, data TEXT)"
        val query =
            "CREATE TABLE IF NOT EXISTS $TABLENAME_config ($config_Id INTEGER PRIMARY KEY AUTOINCREMENT,$config_Key TEXT ,$config_Value TEXT );"
        val createReqTableQuery = """
            CREATE TABLE IF NOT EXISTS $REQ_TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_URL TEXT,
                $COLUMN_POST_DATA TEXT,
                $COLUMN_METHOD TEXT,
                $COLUMN_LAST_PROCESSED_AT TIMESTAMP,
                $COLUMN_STATUS TEXT DEFAULT 'not_processed'
            )
        """.trimIndent()

        val createCBTableQuery = """
            CREATE TABLE IF NOT EXISTS $CB_TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_URL TEXT,
                $COLUMN_POST_DATA TEXT,
                $COLUMN_METHOD TEXT,
                $COLUMN_LAST_PROCESSED_AT TIMESTAMP,
                $COLUMN_STATUS TEXT DEFAULT 'not_processed'
            )
        """.trimIndent()

        try {
            db.execSQL(query)
            db.execSQL(createTableQuery)
            db.execSQL(createReqTableQuery)
            db.execSQL(createCBTableQuery)
        } catch (e: Exception) {
            Logger.e("db", "onCreate - $e", e)
        }
    }

    override fun close() {
        db.close()
        super.close()
    }

    fun updateStatusAndLastProcessedTime(id:Int?, tableName: String,status:String){
        if (!this.doesTableExist(db, REQ_TABLE_NAME) || !this.doesTableExist(db, CB_TABLE_NAME)) {
            Logger.i(TAG, "Creating tables in updateStatusAndLastProcessedTime")
            this.onCreate(db);
        }
        if(id == 0) {
            return
        }
        val contentValues = ContentValues().apply {
            put(COLUMN_STATUS, status)
            put(COLUMN_LAST_PROCESSED_AT, getCurrentTimestamp())
        }

        val whereClause = "$COLUMN_ID = ?"
        val whereArgs = arrayOf(id.toString())

        db.update(tableName, contentValues, whereClause, whereArgs)
    }

    fun updateAllRequestsToNotProcessed() {
        if (!this.doesTableExist(db, REQ_TABLE_NAME) || !this.doesTableExist(db, CB_TABLE_NAME)) {
            Logger.i(TAG, "Creating tables in updateAllRequestsToNotProcessed")
            this.onCreate(db);
        }
        val contentValues = ContentValues().apply {
            put(COLUMN_STATUS, "not_processed")
            put(COLUMN_LAST_PROCESSED_AT, getCurrentTimestamp())
        }
        db.update(REQ_TABLE_NAME, contentValues, null, null)
        db.update(CB_TABLE_NAME, contentValues, null, null)
    }

    private fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        try {
            Logger.d("db", "onUpgrade -Drop Tables")
            db.execSQL("DROP TABLE IF EXISTS $TABLENAME_config")
            db.execSQL("DROP TABLE IF EXISTS $REQ_TABLE_NAME")
            db.execSQL("DROP TABLE IF EXISTS $CB_TABLE_NAME")
        } catch (e: Exception) {
            Logger.e("db", "onUpgrade - $e", e)
        }
        onCreate(db)
    }

    private fun insert_config(table_model_obj: Config) {
        if (!this.doesTableExist(db, REQ_TABLE_NAME) || !this.doesTableExist(db, CB_TABLE_NAME)) {
            Logger.w(TAG, "Creating tables in ate")
            this.onCreate(db);
        }
        val contentValues = ContentValues()
        contentValues.put(config_Key, table_model_obj.key)
        contentValues.put(config_Value, table_model_obj.value)
        try {
            db.insert(TABLENAME_config, null, contentValues)
        } catch (e: Exception) {
            Logger.e("db", "insert_config - $e", e)
        }
    }

    fun insertRequest(request:RequestHandler.Request, tableName:String, id: Int? = 0){
        if (!this.doesTableExist(db, REQ_TABLE_NAME) || !this.doesTableExist(db, CB_TABLE_NAME)) {
            Logger.i(TAG, "Creating tables in insertRequest")
            this.onCreate(db);
        }
        val values = ContentValues().apply {
            put(COLUMN_URL, request.url)
            put(COLUMN_POST_DATA, request.postData?.toString())
            put(COLUMN_METHOD, request.method)
            put(COLUMN_LAST_PROCESSED_AT, getCurrentTimestamp())
        }

        if (id != 0){
            values.put(COLUMN_ID, id)
        }

        try {
            db.insert(tableName, null, values)
        } catch (e: Exception) {
            Logger.e(TAG, "insert_requests - $e", e)
        }
    }

    fun deleteRequestByID(id:Int?, tableName: String){
        if (!this.doesTableExist(db, REQ_TABLE_NAME) || !this.doesTableExist(db, CB_TABLE_NAME)) {
            Logger.i(TAG, "Creating tables in deleteRequestByID")
            this.onCreate(db);
        }
        if(id == 0) {
            return
        }
        try {
            db.delete(tableName,"$COLUMN_ID = ?", arrayOf(id.toString()))
        } catch (e: Exception) {
            Logger.e("db", "deleteRequestByID - $e", e)
        }
    }

    fun getNextRequest(tableName: String): Cursor {
        if (!this.doesTableExist(db, REQ_TABLE_NAME) || !this.doesTableExist(db, CB_TABLE_NAME)) {
            Logger.i(TAG, "Creating tables in getNextRequest")
            this.onCreate(db);
        }
        return db.query(
            tableName,
            null,
            "$COLUMN_STATUS = \'not_processed\'",
            null,
            null,
            null,
            "$COLUMN_ID ASC",
            "1"
        )
    }

    fun insertConfigByKey(table_model_obj: Config) {
        if (!this.doesTableExist(db, REQ_TABLE_NAME) || !this.doesTableExist(db, CB_TABLE_NAME)) {
            Logger.i(TAG, "Creating tables in insertConfigByKey")
            this.onCreate(db);
        }
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
                        Logger.d("db", "insert_configByKey - $e")
                    }
                }
            } else {
                c.close()
                insert_config(table_model_obj)
            }
        } catch (e: Exception) {
            Logger.e("db", "insert_configByKey - $e", e)
        } finally {
            c?.close()
        }
    }


    fun getConfigByKey(key: String): Config {
        if (!this.doesTableExist(db, REQ_TABLE_NAME) || !this.doesTableExist(db, CB_TABLE_NAME)) {
            Logger.i(TAG, "Creating tables in getConfigByKey")
            this.onCreate(db);
        }
        val log = Config()
        var c: Cursor? = null
        try {
            val query =
                "select * from $TABLENAME_config where $config_Key ='$key'"
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
            Logger.e("db", "getConfigByKey - $e", e)
        } finally {
            c?.close()
        }
        return log
    }

    fun deleteAllConfigs() {
            if (!this.doesTableExist(db, REQ_TABLE_NAME) || !this.doesTableExist(db, CB_TABLE_NAME)) {
                Logger.i(TAG, "Creating tables in deleteAllConfigs")
                this.onCreate(db);
            }
        try {
            db.execSQL("DELETE FROM $TABLENAME_config")
        } catch (e: Exception) {
            Logger.e("db", "deleteAllConfigs - $e", e)
        }
    }

    private fun doesTableExist(db: SQLiteDatabase, tableName: String): Boolean {
        val cursor = db.rawQuery(
            "select DISTINCT tbl_name from sqlite_master where tbl_name = '$tableName'",
            null
        )
        if (cursor != null) {
            if (cursor.count > 0) {
                cursor.close()
                return true
            }
        }
        cursor.close()
        return false
    }

    companion object {
        private const val DATABASE_NAME = "fyno.db"
        private const val DATABASE_VERSION = 1
        private const val TABLENAME_config = "config"
        private const val config_Id = "id"
        private const val config_Key = "key_name"
        private const val config_Value = "value"
        private const val TAG = "database_log"
        const val REQ_TABLE_NAME = "requests"
        const val CB_TABLE_NAME = "callbacks"
        const val COLUMN_ID = "id"
        const val COLUMN_URL = "url"
        const val COLUMN_POST_DATA = "post_data"
        const val COLUMN_METHOD = "method"
        const val COLUMN_LAST_PROCESSED_AT = "last_processed_at"
        const val COLUMN_STATUS = "status"
    }
}
