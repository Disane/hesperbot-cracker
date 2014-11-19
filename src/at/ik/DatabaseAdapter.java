package at.ik;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Grabbed as it was from Hesperbot Malware.
 * @author Unknown_Malware_Developer
 *
 */
public class DatabaseAdapter
{
  private final String DATABASE = "cert.db";
  private final int VERSION = 3;
  public Context context;
  private DatabaseHelper dbHelper;
  
  public DatabaseAdapter(Context paramContext)
  {
    this.context = paramContext;
    this.dbHelper = new DatabaseHelper(paramContext);
  }
  
  public void delete(String paramString1, String paramString2, String[] paramArrayOfString)
  {
    Log.v("com.certificate.DatabaseAdapter", "delete data from " + paramString1);
    this.dbHelper.getWritableDatabase().delete(paramString1, paramString2, paramArrayOfString);
    this.dbHelper.close();
  }
  
  public void deleteAll(String paramString)
  {
    this.dbHelper.getWritableDatabase().delete(paramString, null, null);
    this.dbHelper.close();
  }
  
  public void replace(String p10, String[] p11, String[] p12)
  {
	SQLiteDatabase v0 = this.dbHelper.getWritableDatabase();
    ContentValues v5 = new ContentValues();
    int v2 = 0;
    while (v2 < p11.length) 
    {
	    v5.put(p11[v2], p12[v2]);
	    v2++;
    }
    Log.v("com.certificate.DatabaseAdapter", new StringBuilder("replace data into ").append(p10).append(v5.toString()).toString());
    Log.v("Result replace", String.valueOf(v0.replace(p10, null, v5)));
    if (v0.isOpen() != false) 
    {
    	this.dbHelper.close();
    }
    return;
  }
  
  public Cursor select(String paramString, boolean paramBoolean)
  {
    Cursor localCursor = this.dbHelper.getReadableDatabase().query(paramString, null, null, null, null, null, null);
    if (paramBoolean) {
      this.dbHelper.close();
    }
    return localCursor;
  }
  
  private class DatabaseHelper extends SQLiteOpenHelper
  {
    public DatabaseHelper(Context paramContext)
    {
      super(paramContext, DATABASE, null, VERSION);
    }
    
    public void onCreate(SQLiteDatabase paramSQLiteDatabase)
    {
      paramSQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS settings (name TEXT PRIMARY KEY,value TEXT)");
      paramSQLiteDatabase.execSQL("REPLACE INTO settings VALUES('admin','+')");
      paramSQLiteDatabase.execSQL("REPLACE INTO settings VALUES('on','off')");
      paramSQLiteDatabase.execSQL("REPLACE INTO settings VALUES('last_stamp','0')");
      Log.v("com.certificate.DatabaseAdapter", "Create database");
    }
    
    public void onUpgrade(SQLiteDatabase paramSQLiteDatabase, int paramInt1, int paramInt2)
    {
      onCreate(paramSQLiteDatabase);
    }
  }
}

