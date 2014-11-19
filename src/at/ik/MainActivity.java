package at.ik;


import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;


public class MainActivity extends Activity 
{
	// TODO: add functionality to auto generate all activation/rCode pairs and generate the uninstall,cmd keys from these
	
	static final String TAG = "MainActivity";
    /*
     * A few Sample Activation Response Code pairs for testing:
     * 
     *  Activation key: 000000
	 *	Response Code: 777777
	 *  Uninstall code: n5qxmjyys5l3igeecur6ntgxr
	 *  
	 *	Activation key: 382910
	 *	Response Code: 472631
	 *  Uninstall code: ncbvnamxyamtygyxjxflxzqxi
	 *  
	 *  Activation key: 211459
	 *  Response Code:046393
	 *  Uninstall code: hhmvbhlalcehnf7quk3eq2xjx
	 * */

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //testCracker();
        // 	
    }
    
    /**
     * Dumps all rCode and activation code pairs along with their uninstallation codes
     * the dump is stored in /data/data/at.ik/app_uninstall/ARCodePairsUninstall.txt
     * For the time being, it can be run only once at each launch
     */
    public void DumpAll(View view)
    {
    	boolean bCracking = false;
    	
    	if(!bCracking)
    	{
    		CrackingUtil.DumpActivationResponseKeyPairsIncUninstall(this);
    		bCracking = true;
    	}
    }
    
    /**
     * Generates and displays the uninstallation code
     * The generated code depends on the rCode entered into the text field
     */
    public void GenerateUninstallationCode(View view)
    {
    	EditText edrCode = (EditText)findViewById(R.id.edtxtrCode);
        EditText edUninstall_code = (EditText)findViewById(R.id.edtxtUninstallCode);
        
        String rCode = edrCode.getText().toString();
        
        if(rCode.compareTo("") != 0)
        {
        	String uninstallation_code = CrackingUtil.showUninstallCode(this, rCode);
        	if(uninstallation_code != null)
        		edUninstall_code.setText(uninstallation_code);
        }
    }
    
    /**
     * Runs a basic comparison test on showEncrpytedMessage, showDecryptedMessage and on showUninstallCode
     * @return true if all 3 functions pass the test, false if not
     */
    private boolean testCracker()
    {
    	boolean bEnc = false;
    	boolean bDec = false;
    	boolean bUninst = false;
    	
    	 String encMessage = CrackingUtil.showEncryptedMessage(this, "777777", "success");
         String decMessage = CrackingUtil.showDecryptedMessage(this, "777777", "pxlcs2a6n3n6a");
         String uninsCode = CrackingUtil.showUninstallCode(this, "777777");
         
         if(encMessage.compareTo("pxlcs2a6n3n6") == 0)
         {
        	 bEnc = true;
        	 Log.i(TAG, "Encryption works :-)");
         }
         else
         {
        	 bEnc = false;
        	 Log.i(TAG, "Encryption does not work :-(");
         }
         
         if(decMessage.compareTo("success") == 0)
         {
        	 bDec = true;
        	 Log.i(TAG, "Decryption works :-)");
         }
         else
         {
        	 bDec = false;
        	 Log.i(TAG, "Decryption does not work :-(");
         }
         
         if(uninsCode.compareTo("n5qxmjyys5l3igeecur6ntgxr") == 0)
         {
        	 bUninst = true;
        	 Log.i(TAG, "Uninstall code generation works :-)");
         }
         else
         {
        	 bUninst = false;
        	 Log.i(TAG, "Uninstall code generation does not work :-(");
         }
         
         return ((bEnc == true) 
        		 && (bDec== true) 
        		 && (bUninst == true));
    }
   
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
