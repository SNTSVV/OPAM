package lu.uni.svv.PriorityAssignment.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;


public class GAWriter {
	/**
	 * Static path setting, the init function should be excuted at the beginning
	 */
	private static String basePath = "";
	public static void setBasePath(String _path){ basePath = _path; }
	public static String getBasePath(){ return GAWriter.basePath; }
	
	public static void init(String _path, boolean makeEmpty){
		// Only apply to multi run mode
		if (_path == null){
			_path = Settings.BASE_PATH;
			if (Settings.RUN_NUM != 0){
				_path += String.format("/Run%02d", Settings.RUN_NUM);
			}
		}
		basePath = _path;
		
		if (makeEmpty) {
			File dir = new File(basePath);
			if (dir.exists()) {
				try {
					FileUtils.deleteDirectory(dir);
				} catch (IOException e) {
					System.out.println("Failed to delete results");
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * Class members
	 */
	private BufferedWriter logger = null;
	private Level level = Level.INFO;
	
	public GAWriter(String _filename, Level _level, String _title) {
		this(_filename, _level, _title,null, false);
	}
	
	public GAWriter(String _filename, Level _level,  String _title, String _path){
		this(_filename, _level, _title, _path, false);
	}
	
	public GAWriter(String _filename, Level _level, String _title, String _path, boolean _append) {
		this.level = _level;

		if (_path == null){
			_path = basePath;
		}
		
		String filename = "";
		if (_path == null || _path.length()==0)
			filename = _filename;
		else
			filename = _path+"/"+_filename;
		File fileObj = new File(filename);
		
		// Create parent folder
		File parent = fileObj.getParentFile();
		if (!parent.exists()){
			if (!fileObj.getParentFile().mkdirs()) {
				System.out.println("Creating folder error: "+fileObj.getAbsolutePath());
				System.exit(1);
			}
		}
		
		// create output file
		boolean flagTitle = false;
		if (!fileObj.exists() && _title != null) flagTitle = true;
		
		FileWriter fw = null;
		try {
			fw = new FileWriter(fileObj.getAbsolutePath(), _append);
			logger = new BufferedWriter(fw);
			if (flagTitle){
				this.info(_title);
			}
		} catch (IOException e) {
			e.printStackTrace();

		} finally {
		}
	}
	
	public void close()
	{
		try {
			logger.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	public void print(String msg) {
		if (!(level==Level.INFO || level==Level.FINE)) return;
		try { 
			System.out.print(msg);
			logger.write(msg);
			logger.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(String msg) {
		try {
			logger.write(msg);
			logger.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void info(String msg) {
		if (!(level==Level.INFO || level==Level.FINE)) return;
		try { 
			logger.write(msg+"\n");
			logger.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void fine(String msg) {
		if (!(level == Level.FINE)) return;
		try {
			logger.write(msg + "\n");
			logger.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
