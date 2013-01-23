package nl.org.mackatack.GoogleDriveDesktopIntegrator;


import java.awt.Color;
import java.awt.FlowLayout;
import java.io.File;
import java.io.IOException;

import javax.swing.JApplet;
import javax.swing.JLabel;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;




/**
 * IntegratorApplet
 * A simple applet that allows users to open local files directly from their browser.
 * In combination with the GoogleDriveIntegrator greasemonkey userscript, the google drive desktop app
 * and your Google Drive web application this allows you to open .ai, .doc, .odt (and more) files directly
 * with your locally installed applications.
 * 
 * This does however require you to already have the files synced locally using the Google Drive Application
 * 
 * So basically all this does is saving you the trouble of having to navigate your folders locally
 * 
 * Since Firefox doesn't allow us to call the java applet functions from javascript, but does allow us to call
 * javascript methods from the applet, lets make a message poller. Lets check ask javascript if there's new data
 * for us to handle at a small interval.
 *
 * @author Mackatack
 * @since 2013
 *
 */
public class IntegratorApplet extends JApplet {

	private static final long serialVersionUID = -1399492154769974680L;
	JLabel lblStatus = new JLabel("GoogleDriveDesktopIntegrator");	
	MessagePoller poller = null;

	/**
	 * Output some status info
	 * @param text
	 */
	public void setStatus(String text) {
		System.out.println(text);
	}
	
	private boolean fileExists(File f) {
		return f.exists() || f.isFile() || f.getAbsoluteFile().isFile() || f.getAbsoluteFile().exists();	
	}
	private boolean fileExists(String f) {
		return fileExists(new File(f));		
	}
	
	@Override
	public void stop() {
		super.stop();
		System.out.println("IntegratorApplet stop()");
		if (poller != null) poller.requestStop();
	}	
	
	@Override
	public void start() {
		super.start();		
		System.out.println("IntegratorApplet start()");	
	}
	
	/**
	 * Open the file using cmd or xdg-open
	 * @param file
	 */
	private void runFile(String file) {
		// Check the OS
		String os = System.getProperty("os.name").toLowerCase();
		
		try {
			// Windows? Lets's use cmd; Linux? use xdg-open
			if (os.indexOf("win") >= 0)
				Runtime.getRuntime().exec(new String[]{"cmd", "/c", file});
			else
				Runtime.getRuntime().exec(new String[]{"xdg-open", file});
		} catch (IOException e) {
			System.out.println("Failed starting file");
			e.printStackTrace();
		}
	}
	
	/**
	 * Loop through all the available path names and open the first file
	 * available on the filesystem
	 * @param openFilenames
	 * @return
	 */
	private boolean findFirstExistentPathAndRun(String openFilenames) {
		// Find the users home dir
		String home = System.getProperty("user.home") + "/Google Drive";
		if (!fileExists(home)) {
			System.out.println("Default homedir does not exist");								
			home = "E:/Google Drive";
		}
		
		// A file could have multiple paths, so lets try all of them and
		// open the first file path we can find on the local filesystem
		String localFile = null;
		String[] fileNames = openFilenames.split("\n");
		for(String fName: fileNames) {
			localFile = home + fName;
			System.out.println("Testing '" + localFile + "'");							
			if (fileExists(localFile)) break;							
			localFile = null;								
		}
		
		// Did we find any of the local paths?
		if (localFile != null) {
			runFile(localFile);
			setStatus("done opening " + localFile);
			return true;
		} else {
			System.out.println("None of the paths could be found");
			return false;
		}
	}

	/**
	 * Main constructor
	 */
	public IntegratorApplet() {
		System.out.println("IntegratorApplet constructor");
		
		setBackground(Color.black);
		getContentPane().setBackground(Color.black);
		
		// Add the status label to the interface
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		lblStatus.setForeground(Color.LIGHT_GRAY);
		add(lblStatus);
		
		poller = new MessagePoller();		
	}
	
	/**
	 * A wrapper function that makes it easier for us to perform javascript function calls
	 * @param functionName
	 * @param args
	 * @return
	 */
	public Object doJavascriptCall(String functionName, Object[] args) {
		if (args == null)
			args = new Object[]{};
		try {
			System.out.println("doJavascriptCall: " + functionName);
			JSObject window = JSObject.getWindow(IntegratorApplet.this);
			return window.call(functionName, args);			
		} catch(Exception e) {
			System.out.println("doJavascriptCall error: " + e.toString());
			e.printStackTrace();
			return null;
		}
	}	

	/**
	 * This thread periodically checks if there's something for us to open
	 * This is done by calling the GoogleDriveDesktopIntegratorPoll javascript function
	 * this function returns a String or Null. If not null the string contains a linebreak
	 * separated list of paths leading to the file the user wants to open
	 * @author Mackatack
	 *
	 */
	class MessagePoller extends Thread {
		boolean stop = false;
		public MessagePoller() {
			setDaemon(true);
			start();
		}
		public void requestStop() {
			stop = true;
		}
		@Override
		public void run() {
			System.out.println("MessagePoller thread started");
			
			// Lets first tell javascript that we're up and running by
			// calling the GoogleDriveDesktopIntegratorInit javascript function
			doJavascriptCall("GoogleDriveDesktopIntegratorInit", null);
			
			// Keep looping
			while(!stop) {
				
				// call the GoogleDriveDesktopIntegratorPoll javascript function and see
				// if there's something for us to open
				try {
					Object path = doJavascriptCall("GoogleDriveDesktopIntegratorPoll", null);
					
					// We've got a file path from the server
					if (path != null) {
						System.out.println("message poller found: " + String.valueOf(path));
						
						// Try to open the first file we find in the path list
						if (findFirstExistentPathAndRun(String.valueOf(path))) {
							// Let javascript know we were able to open the file successfully
							doJavascriptCall("GoogleDriveDesktopIntegratorSuccess", null);
						} else {
							// Let javascript know we were unable to open the file
							doJavascriptCall("GoogleDriveDesktopIntegratorFail", null);
						}						
					}
				} catch (JSException jsE) {
					System.out.println("Stopping poller due to javascript error: " + jsE.toString());
					jsE.printStackTrace();
					break;				
				} catch(Exception e) {
					System.out.println("GoogleDriveDesktopIntegratorPoll error: " + e.toString());
					e.printStackTrace();
				}
				
				// Sleep one second until polling again
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {}				
			}
			System.out.println("MessagePoller thread stopped");
		}
	}
}