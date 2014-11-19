package at.ik;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import dalvik.system.DexClassLoader;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * NOTE: In order to make this code work properly, it requires the infected DEX file (classes.dex renamed to hesperbot.dex) and its database (cert.db)
 * I used the following sample:
 * 
 * DEX Sample:
 *		o	MD5: 3d70ebfce0130c08772bf449d82d1235
 *		o	SHA1: 8fce71267af12db8578c9676c58ecf6c2c3d0424
 *		o	SHA256: 9f9930e0eced0d3ba610ff381f0c1e60cceb63e8f64969026ef5c466dc8038ab
 *
 * 
 *
 * @author Tibor Éliás, BSc
 *
 */
public class HesperbotCracker
{
	static final String TAG = "HesperbotCracker";
	private DexClassLoader dcl;
	private Context ctx;
	private static Class<?> clsMobem = null;
	private static HesperbotCracker myHesperbotCracker = null;
	
	/**
	 * Creates an instance of HesperbotCracker 
	 * or provides one if already available.
	 * @param ctx - the caller application's context
	 * @return an instance of Hesperbot
	 */
	public static HesperbotCracker getInstance(Context ctx)
	{
		if(myHesperbotCracker == null)
		{
			myHesperbotCracker = new HesperbotCracker(ctx);
		}
		return myHesperbotCracker;
	}
	
	/**
	 * The Contructor loads the DEX file and the database of the Hesperbot
	 * and then it loads the "mobem" class from the DEX file.
	 * @param ctx - the caller application's context
	 */
	private HesperbotCracker(Context ctx)
	{
		this.ctx = ctx;
		
		// Create local copy of DEX for manipulation
		File dexInternalStoragePath = aquireHesperbotDex();
		
		// Create a copy of the cert.db database in the /databases/ folder
		aquireHesperbotDatabase();
		
		// Load Hesperbot classes
		// Internal storage where the DexClassLoader writes the optimized DEX file to
		if(dexInternalStoragePath != null)
		{
	        final File optimizedDexOutputPath = ctx.getDir("outdex", Context.MODE_PRIVATE);
	        dcl = new DexClassLoader(dexInternalStoragePath.getAbsolutePath(),
	        						optimizedDexOutputPath.getAbsolutePath(),
	        						null,ctx.getClassLoader());
	        if(dcl == null)
	        {
	        	Log.e(TAG, "Failed to load hesperbot classes, using the DEX class loader!");
	        }
		}
		else
		{
			Log.e(TAG, "Failed to load hesperbot classes, because of missing DEX file!");
		}
		
		try 
	    {
			// Load the Hesperbot library.
			clsMobem = dcl.loadClass("com.mobem.controller.mobem");
	    }
	    catch (Exception exc) 
        { 
        	Log.e(TAG,exc.getMessage());
        }
	}
	
	/**
	 * This member function of the "mobem" class detirmines whether 
	 * cval is between min and max. 
	 * For example:
	 * 17 > 2 || 17 <16 => false
	 * 
	 * @param cval - the value, which is compared to min and max
	 * @param min - the minimum value, which is compared to cval
	 * @param max - the maximum value, which is compared to cval
	 * @return if cval is smaller or larger than max the return value is false, otherwise true
	 */
	public boolean runIN_RANGE(int cval, int min, int max)
	{
		 boolean res = false;
		 try 
	     {
	    	// ---------------------- Hax TEST: See if we can call a method for testing
	    	// public static boolean IN_RANGE(int currentVal, int minVal, int maxVal)
	    	Method methMobem = clsMobem.getMethod("IN_RANGE",int.class,int.class,int.class);
	    	
	    	// static call (=no need for receiver)
	    	Object result = (Object)methMobem.invoke(null, cval,min,max);
	    	res = ((Boolean)result).booleanValue();
	    	
	    	//Log.v(TAG,"Method call success: " + res);
	    }
        catch (Exception exc) 
        { 
        	Log.e(TAG,exc.getMessage());
        }
		return res;
	}
	
	@SuppressWarnings("unchecked")
	/***
	 * Used for decrypting messages using an rCode and a message string that needs to be decrypted.
	 * 
	 * Instantiates one of Hesperbot's DatabaseAdapter object to gain access to its database.
	 * The database object then creates a new Cache object and takes care of the initialization of
	 * the cipher key and the loading of Malware settings from its database.
	 * 
	 * NOTE: these code parts do not activate the ActicationActivity or the LogFilterService,
	 * therefore the SMS based C&C server and SMS forwarding will not become active.
	 * 
	 * The local HashMap that stores the settings is overwritten by the rCode parameter.
	 * Additionally the loaded variable is set to zero, 
	 * this way the Malware belives that it has not been initialized yet.
	 * This way cipher key will initialize it self using the provided rCode.
	 * 
	 * In order to successfully call the mobem_decode_text() member function, 
	 * we acquire an instance to the current Cache and also an instance to the current cipher key.
	 * mobem_decode_text() is dynamically looked up and called on the message that needs to be decrypted.
	 * 
	 * @param rCode - the response code, which is fed to the current Cache object and used for generating the cipher key
	 * 				  (BufferedBlockCipher)
	 * @param message - The message that needs to be decrypted
	 * @return decrypted message
	 */
	public String decryptMessage(String rCode, String message)
	{
		 // stores the decrypted message
		 String strDecryptedMessage = null;
		 
		 try 
	     {        	
        	// create a new DatabaseAdapter (custom class for reading the cert.db database)
        	Class <?> clsDatabaseAdapter = dcl.loadClass("com.certificate.DatabaseAdapter");
        	
        	// acquire constructor DataAdapter(Context ctx)
        	Constructor<?> DatabaseAdapterConstructor = clsDatabaseAdapter.getConstructor(Context.class);
        	
        	// instantiate DataBaseAdapter using the current Context, 
        	// NOTE that the database cert.db from the HesperbotNexusOnex86 emulator was moved into /databases/cert.db 
        	Object instDatabaseAdapter = DatabaseAdapterConstructor.newInstance(ctx);
        	
        	// attempt to initialize the cipher key using the database pulled from HesperbotNexusOnex86 emulator
        	Method methDataAdapterloadCache = clsDatabaseAdapter.getMethod("loadCache");
        	Object localCache = methDataAdapterloadCache.invoke(instDatabaseAdapter);
        	
        	
        	// Accessing public (!) hash map of settings to change the rCode
        	Field ReflectionSettingsHashMap = localCache.getClass().getField("settings");
        	HashMap<String,String> settingsHashMap = null;
        	try
        	{
        		// acquire the settings HashMap for writing the local settings in the Malware's memory
        		Object settingsObject = ReflectionSettingsHashMap.get(localCache);
        		settingsHashMap = (HashMap<String, String>)settingsObject;
        		
        		if (settingsHashMap.containsKey("rCode"))
        		{
        			settingsHashMap.remove("rCode");
        			
        			// overwrite the current rCode with the one that was provided as a parameter
        			settingsHashMap.put("rCode", rCode);
        			
        			// reset the "loaded" variable, setting the Malware to uninitialized state
        			Field ReflectionSettingsLoaded = localCache.getClass().getField("loaded");
        			ReflectionSettingsLoaded.setInt(localCache,0);
        		}
        	}
        	catch(Exception ex)
        	{
        		Log.e("TAG",ex.getMessage());
        	}
        	
        		
        	// acquire Cache class
        	Field RefMobemInstance = localCache.getClass().getField("mobemInstabce");
        	
        	// acquire the cipher key's class (BufferedBlockCipher)
        	Field RefMobemCipher = localCache.getClass().getField("cipher");
        	
        	// acquire mobem instance from the Cache class
        	Object MobemInstance = RefMobemInstance.get(localCache);
        	
        	// acquire the instance of the cipher key
        	Object MobemCipher = RefMobemCipher.get(localCache);
        	
        	Object decryptedMessage = null;
        	
        	// search for the "mobem_decode_text" function
        	for(Method Mmobem_decode_text : MobemInstance.getClass().getMethods())
        	{
        		if(Mmobem_decode_text.getName().compareTo("mobem_decode_text") == 0)
        		{
        			// when it is found, call it using the current cipher key with the text that needs to be decrypted 
        			// and return the results to the mobem object
        			decryptedMessage= (String)Mmobem_decode_text.invoke(MobemInstance, MobemCipher, message.toCharArray());
        		}
        	}
        	
        	// if the decryption was successful
        	if(decryptedMessage != null)
        	{
        		// convert the returned object to a String
				strDecryptedMessage = (String)decryptedMessage;
				
				// remove all empty space characters from the string
				strDecryptedMessage = strDecryptedMessage.replace(" ","");
        	}
        } 
        catch (Exception exc) 
        { 
        	Log.e(TAG,exc.getMessage());
        }
		
		if (strDecryptedMessage != null)
			return strDecryptedMessage;
		else
			return null;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Used for encrypting messages using an rCode and a message string that needs to be encrypted.
	 * 
	 * Instantiates one of Hesperbot's DatabaseAdapter object to gain access to its database.
	 * The database object then creates a new Cache object and takes care of the initialization of
	 * the cipher key and the loading of Malware settings from its database.
	 * 
	 * NOTE: these code parts do not activate the ActicationActivity or the LogFilterService,
	 * therefore the SMS based C&C server and SMS forwarding will not become active.
	 * 
	 * The local HashMap that stores the settings is overwritten by the rCode parameter.
	 * Additionally the loaded variable is set to zero, 
	 * this way the Malware belives that it has not been initialized yet.
	 * The cipher key will initialize it self using the provided rCode. 
	 * 
	 * @param rCode - the response code, which is fed to the current Cache object and used for generating the cipher key
	 * 				  (BufferedBlockCipher)
	 * @param message - The message that needs to be decrypted
	 * @return decrypted message
	 */
	public String encryptMessage(String rCode, String message)
	{
		 String strEncryptedMessage = null;
		 try 
	     {       	
        	// create a new DatabaseAdapter (custom class for reading the cert.db database)
        	Class <?> clsDatabaseAdapter = dcl.loadClass("com.certificate.DatabaseAdapter");
        	//Log.v(TAG,"Class " + clsDatabaseAdapter.getName() + " is loaded!");
        	
        	// acquire constructor DataAdapter(Context ctx)
        	Constructor<?> DatabaseAdapterConstructor = clsDatabaseAdapter.getConstructor(Context.class);
        	
        	// instantiate DataBaseAdapter using the current Context, 
        	// NOTE that the database cert.db from the HesperbotNexusOnex86 emulator was moved into /databases/cert.db 
        	Object instDatabaseAdapter = DatabaseAdapterConstructor.newInstance(ctx);
        	
        	// attempt to initialize the cipher key using the database pulled from HesperbotNexusOnex86 emulator
        	Method methDataAdapterloadCache = clsDatabaseAdapter.getMethod("loadCache");
        	Object localCache = methDataAdapterloadCache.invoke(instDatabaseAdapter);
        	
        	// Accessing public (!) hash map of settings
        	Field ReflectionSettingsHashMap = localCache.getClass().getField("settings");
        	HashMap<String,String> settingsHashMap = null;
        	try
        	{
        		// acquire the settings HashMap for writing the local settings in the Malware's memory
        		Object settingsObject = ReflectionSettingsHashMap.get(localCache);
        		settingsHashMap = (HashMap<String, String>)settingsObject;
        		
        		if (settingsHashMap.containsKey("rCode"))
        		{
        			settingsHashMap.remove("rCode");
        			
        			// overwrite the current rCode with the one that was provided as a parameter
        			settingsHashMap.put("rCode", rCode);
        			
        			// reset the "loaded" variable, setting the Malware to uninitialized state
        			Field ReflectionSettingsLoaded = localCache.getClass().getField("loaded");
        			ReflectionSettingsLoaded.setInt(localCache,0);
        		}
        	}
        	catch(Exception ex)
        	{
        		Log.e("TAG",ex.getMessage());
        	}
        	
        		
        	// attempt to create a new LogFilterService
        	Class <?> clsUtil = dcl.loadClass("com.certificate.Util");

        	// Acquire the EncodeThis() member function of the static class utility
        	Method methUtilEncodeThis = clsUtil.getDeclaredMethod("EncodeThis",String.class);
        	
        	// if the method was found
        	if (methUtilEncodeThis != null)
        	{
        		// TODO bypass the private qualifier
	        	//methUtilEncodeThis.setAccessible(true);
	        	
	        	try
	        	{
	        		// call the EncodeTihs() member function and pass the message that needs to be encoded
	        		Object encryptedMessage = (Object)methUtilEncodeThis.invoke(null, message);
	        		
	        		strEncryptedMessage = (String)encryptedMessage;
		        	
	        		// remove all empty space characters from the string 
	        		strEncryptedMessage = strEncryptedMessage.replace(" ","");
	        	}
	        	catch(NullPointerException NullExcp)
	        	{
	        		Log.e(TAG,NullExcp.getMessage());
	        	}
	        	catch (IllegalAccessException illegalAccessExcp) 
	        	{
	        		Log.e(TAG,illegalAccessExcp.getMessage());
	        	}
	        	catch (IllegalArgumentException illegalArgumentExcp)
	        	{
	        		Log.e(TAG,illegalArgumentExcp.getMessage());
	        	}
	        	catch(InvocationTargetException InvocationTargetExcp)
	        	{
	        		Log.e(TAG,InvocationTargetExcp.getMessage());
	        	}
        	}
        } 
        catch (Exception exc) 
        { 
        	Log.e(TAG,exc.getMessage());
        }
		
		if (strEncryptedMessage != null)
			return strEncryptedMessage.substring(0, strEncryptedMessage.length() - 1);
		else
			return null;
	}
	
	/**
	 * Using the reverse engineered DatabaseAdapter, 
	 * replace the old rCode, with the one provided as a parameter in the cert.db database.
	 * @param rCode - the new rCode for replacing the old one in cert.db
	 */
	public void overwriteResponseCode(String rCode)
	{
		if(dcl != null)
		{
			// Attempt to overwrite the database in the file system
        	DatabaseAdapter myDatabaseAdapter = new DatabaseAdapter(ctx);
        	myDatabaseAdapter.replace("settings", new String[] { "name", "value" }, new String[] { "rCode", rCode });
		}
	}
	
	/**
	 * Makes a copy of cert.db inside the /databases directory for processing
	 */
	private void aquireHesperbotDatabase()
	{
		String databaseCertDB = new String("cert.db");
		
		File hesperbotDatabasesPath = new File("//data//data//at.ik//databases//");
        AssetManager assetsMan = ctx.getAssets();
       
        File databasesInternalStoragePath = new File("//data//data//at.ik//databases//cert.db");
        OutputStream certWriter = null;
        Log.v(TAG, "databases directory created!");
        
        if(databasesInternalStoragePath.exists())
        {
    		 int BUF_SIZE = 100 * 1024;
	    	 try 
	         {
	         	 if (!hesperbotDatabasesPath.exists())
	              {
	         		 hesperbotDatabasesPath.mkdirs();
	              }
	         	
	         	InputStream databaseCertDBFile = assetsMan.open(databaseCertDB);
	         	certWriter = new BufferedOutputStream(new FileOutputStream(databasesInternalStoragePath));
	             byte[] buf = new byte[BUF_SIZE];
	             int len;
	             while((len = databaseCertDBFile.read(buf, 0, BUF_SIZE)) > 0) {
	             	certWriter.write(buf, 0, len);
	             }
	             certWriter.close();
	             databaseCertDBFile.close();
	             Log.v(TAG, "reading " + databaseCertDB);
	         }
	         catch (Exception exc)
	         {
	         	Log.v(TAG,exc.getMessage());
	         }
        }
		
	}
	
	/**
	 * Makes a copy of the hesperbot.dex inside the /app_dex directory
	 * @return hesperbot.dex internal storage path
	 */
	private File aquireHesperbotDex()
	{
		String SECONDARY_DEX_NAME = "hesperbot.dex";
		File dexInternalStoragePath = new File(ctx.getDir("dex", Context.MODE_PRIVATE),SECONDARY_DEX_NAME);
		Log.v(TAG, "DEX directory created!");
		
		InputStream dexReader = null;
		OutputStream dexWriter = null;
		
		if(!dexInternalStoragePath.exists())
		{
			int BUF_SIZE = 100 * 1024;
	        try 
	        {
	        	dexReader = new BufferedInputStream(ctx.getAssets().open(SECONDARY_DEX_NAME));
	            dexWriter = new BufferedOutputStream(new FileOutputStream(dexInternalStoragePath));
	            byte[] buf = new byte[BUF_SIZE];
	            int len;
	            while((len = dexReader.read(buf, 0, BUF_SIZE)) > 0) {
	                dexWriter.write(buf, 0, len);
	            }
	            dexWriter.close();
	            dexReader.close();
	            Log.v(TAG, "reading " + SECONDARY_DEX_NAME);
	        }
	        catch (Exception exc)
	        {
	        	Log.v(TAG,exc.getMessage());
	        }
		}
		return dexInternalStoragePath;
	}
}
