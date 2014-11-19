package at.ik;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

/**
 * 
 * This is a helper class for calling some of the more useful functionalities of the HesperbotCracker class.
 * 
 * @author Éliás Tibor. BSc
 *
 */
public class CrackingUtil 
{
	public final static String TAG = "CrackUtil";
	
	/**
	 * Dynamically calculates the next valid activation code and response code pairs
	 * and then it uses the response code with the string "uninstall" to calculate 
	 * the uninstallation code.
	 * 
	 * the activation code as well as the response code along with the uninstallation code
	 * are dumped into /uninstallcodes/ARCodePairsIncUninstall.txt
	 * 
	 * @param ctx - the caller application's context
	 */
	public static void DumpActivationResponseKeyPairsIncUninstall(Context ctx)
    {
    	CodeGenerator code_gen = new CodeGenerator();
        String uninstallcodesFile = "ARCodePairsIncUninstall.txt";
        File arcodesFileInternalStoragePath = new File(ctx.getDir("uninstallcodes", Context.MODE_PRIVATE),uninstallcodesFile);
        BufferedWriter out = null;
        
        HesperbotCracker cracker;
        try 
        {
			out = new BufferedWriter(new FileWriter(arcodesFileInternalStoragePath));
			if (out != null)
			{
				String curAct = "000000";
				cracker = HesperbotCracker.getInstance(ctx.getApplicationContext());
				while(Integer.parseInt(curAct) < 999992)
	        	{
			        code_gen.generateNextKeyPair();
			        curAct = code_gen.getActivation_code();
			        String curResp = code_gen.getResponse_code();
			        
			        cracker.overwriteResponseCode(curResp);
	        		String uninstallCode = cracker.encryptMessage(curResp, "uninstall");
			        
			        out.write("Activation Code: " + curAct 
			        		+ "\nResponse Code: " + curResp 
			        		+ "\nUninstall Code: " + uninstallCode 
			        		+ "\n");
			        
			        Log.v(TAG,"Activation Code: " + curAct);
			        Log.v(TAG,"Response Code: " + curResp);
			        Log.v(TAG,"Uninstall Code: " + curResp);
	        	}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally
        {
        	if (out != null)
			{
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        }
    }
    
	/**
	 * Dynamically calculates the next valid activation code and response code pairs.
	 * 
	 * the activation code as well as the response code 
	 * are dumped into /uninstallcodes/ARCodePairs.txt
	 * 
	 * @param ctx - the caller application's context
	 */
    public static void DumpActivationResponseKeyPairs(Context ctx)
    {
    	CodeGenerator code_gen = new CodeGenerator();
        String uninstallcodesFile = "ARCodePairs.txt";
        File arcodesFileInternalStoragePath = new File(ctx.getDir("uninstallcodes", Context.MODE_PRIVATE),uninstallcodesFile);
        BufferedWriter out = null;
        try 
        {
			out = new BufferedWriter(new FileWriter(arcodesFileInternalStoragePath));
			if (out != null)
			{
				String curAct = "000000";
				while(Integer.parseInt(curAct) < 999992)
	        	{
			        code_gen.generateNextKeyPair();
			        curAct = code_gen.getActivation_code();
			        
			        String curResp = code_gen.getResponse_code();
			        out.write("Activation Code: " + curAct 
			        		+ "\nResponse Code: " + curResp 
			        		+ "\n");
			        
			        Log.v(TAG,"Activation Code: " + curAct);
			        Log.v(TAG,"Response Code: " + curResp);
	        	}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally
        {
        	if (out != null)
			{
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        }
    }
    
    /**
     * Calculates the next valid activation code and response code pairs.
     * It reads all lines of the Hesperbot_rCodes.txt to calculate each uninstall code.
	 * 
	 * the activation code as well as the response code 
	 * are dumped into /uninstallcodes/hesperbot_uninstallcodes.txt
     * 
     * @param ctx - the caller application's context
     */
    public static void CrackUninstallCodes(Context ctx)
    {
        final String Hesperbot_rCodes = "Hesperbot_rCodes.txt";
        AssetManager assetsMan = null;
        InputStream isHesperbot_rCodes = null;
        BufferedReader breader = null;
        String strLine = null;
        String uninstallCode = null;
        HesperbotCracker cracker;
        BufferedWriter out = null;
        try 
        {
	        assetsMan = ctx.getAssets();
	        isHesperbot_rCodes = assetsMan.open(Hesperbot_rCodes);
	        breader = new BufferedReader(new InputStreamReader(isHesperbot_rCodes));
	        strLine = null;
	        cracker = HesperbotCracker.getInstance(ctx.getApplicationContext());
	        
	        // write all codes into /uninstallcodes/hesperbot_uninstallcodes.txt
	        String uninstallcodesFile = "hesperbot_uninstallcodes.txt";
			File uninstallcodesFileInternalStoragePath = new File(ctx.getDir("uninstallcodes", Context.MODE_PRIVATE),uninstallcodesFile);
			Log.v(TAG, "uninstallcodes directory created!");
			out = new BufferedWriter(new FileWriter(uninstallcodesFileInternalStoragePath));
	        
	        if(isHesperbot_rCodes != null )
	        {
	        	while( (strLine = breader.readLine()) != null)
	        	{
	        		cracker.overwriteResponseCode(strLine);
	        		uninstallCode = cracker.encryptMessage(strLine, "uninstall");
	        		if (uninstallCode != null)
	        		{
		        		Log.v(TAG,"rCode: " + strLine + " Uninstall/Password: " + uninstallCode + "\n");   		
		    			out.write("rCode: " + strLine + " Uninstall/Password: " + uninstallCode + "\n");
	        		}
	        		else
	        			Log.v(TAG,"rCode: " + strLine + "Uninstall/Password: " + "failed to read code!" + "\n");
	        	}
	        } 
		} catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        finally
        {
        	if(isHesperbot_rCodes != null)
        	{
	        	try 
	        	{
					isHesperbot_rCodes.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	
        	if (out != null)
			{
				try {
					out.flush();
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        }
    }
    
    /**
     * 
     * @param ctx - the caller application's context
     * @param rCode - the response code, which is fed to the current Cache object and used for generating the cipher key
	 * 				  (BufferedBlockCipher) 
     * @return
     */
	public static String showUninstallCode(Context ctx, String rCode)
	{
        HesperbotCracker cracker = HesperbotCracker.getInstance(ctx.getApplicationContext());
        cracker.overwriteResponseCode(rCode);
        String uninstallCode = cracker.encryptMessage(rCode, "uninstall");
        Log.v(TAG,"Uninstall code: " + uninstallCode);
        
        return uninstallCode;
	}
	/**
	 * 
	 * @param ctx - the caller application's context
	 * @param rCode - the response code, which is fed to the current Cache object and used for generating the cipher key
	 * 				  (BufferedBlockCipher)
	 * @param message
	 * @return
	 */
	public static String showEncryptedMessage(Context ctx, String rCode, String message)
	{
		HesperbotCracker cracker = HesperbotCracker.getInstance(ctx.getApplicationContext());
        cracker.overwriteResponseCode(rCode);
        String encrypted_message = cracker.encryptMessage(rCode,message);
        Log.v(TAG,"Encrypted message: " + encrypted_message);
        
        return encrypted_message;
	}
	
	/**
	 * 
	 * @param ctx - the caller application's context
	 * @param rCode - the response code, which is fed to the current Cache object and used for generating the cipher key
	 * 				  (BufferedBlockCipher)
	 * @param message
	 * @return
	 */
	public static String showDecryptedMessage(Context ctx, String rCode, String message)
	{
        HesperbotCracker cracker = HesperbotCracker.getInstance(ctx.getApplicationContext());
        cracker.overwriteResponseCode(rCode);
        String decrypted_message = cracker.decryptMessage(rCode,message);
        Log.v(TAG,"Decrypted message: " + decrypted_message);
        
        return decrypted_message;
	}
}
