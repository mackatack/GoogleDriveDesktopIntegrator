package nl.org.mackatack.GoogleDriveDesktopIntegrator;


import java.awt.FlowLayout;
import javax.swing.JApplet;
import javax.swing.JLabel;

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
 * @author Mackatack
 * @since 2013
 *
 */
public class IntegratorApplet extends JApplet {

	private static final long serialVersionUID = 8493678796429149417L;
	String openFilename = null;
	Thread openThread = null;
	Object openThreadLock = new Object();
	JLabel lblStatus = new JLabel("GoogleDriveDesktopIntegrator");

	/**
	 * Output some status info
	 *
	 * @param text
	 */
	public void setStatus(String text) {
		System.out.println(text);
	}

	/**
	 * This public function will be exposed for use with javascript.
	 * Because of security restrictions Java doesn't allow us to run programs
	 * from a tainted program path. We'll use a thread to perform the execute
	 * statements for us.
	 *
	 * @param fileName 	Path to the file we want to open
	 */
	public void openFile(String fileName) {
		setStatus("openFile("+fileName+") aangeroepen, notify thread");
		synchronized (openThreadLock) {
			// Save the unsafe filename so the thread can open it later
			openFilename = fileName;

			// Notify the openThread
			openThreadLock.notifyAll();
		}
	}

	/**
	 * Main constructor
	 */
	public IntegratorApplet() {
		// Add the status label to the interface
		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		add(lblStatus);

		/**
		 * Since we're getting requests from the unsafe browser java doesn't allow us to
		 * run system commands redirectly. We need a Thread to run the commands
		 * 
		 * TODO: create the thread statically, so multiple instances of the applet share this thread.
		 */
		openThread = new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (openThreadLock) {
					// Just leave the thread running forever
					while(true) {
						try {
							// Just wait till openFile() is called and thus the notifyAll()
							openThreadLock.wait();
							setStatus("thread wakeup, opening " + openFilename);

							// Check the OS
							String os = System.getProperty("os.name").toLowerCase();

							// Windows? Lets's use cmd; Linux? use xdg-open
							if (os.indexOf("win") >= 0)
								Runtime.getRuntime().exec(new String[]{"cmd", "/c", openFilename});
							else
								Runtime.getRuntime().exec(new String[]{"xdg-open", openFilename});

							// Done!
							setStatus("done opening " + openFilename);
						} catch (Exception e) {
							setStatus("Error: " + e.getMessage());
							e.printStackTrace();
						}
					}
				}
			}
		});
		// Set the thread type to Daemon, so it doesn't block the applet from closing
		openThread.setDaemon(true);
		
		// Start the thread.
		openThread.start();
	}
}
