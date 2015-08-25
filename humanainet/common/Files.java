/** Ben F Rayfield offers this "common" software to everyone opensource GNU LGPL */
package humanainet.common;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Files{
	private Files(){}

	public static byte[] read(File file) throws IOException{
		if(file.isDirectory()) return new byte[0];
		InputStream fileIn = null;
		try{
			fileIn = new FileInputStream(file);
			byte fileBytes[] = new byte[fileIn.available()];
			int bytesRead = fileIn.read(fileBytes);
			if(bytesRead != fileBytes.length) throw new IOException(
				"Tried to write "+fileBytes.length+" bytes but did write "+bytesRead+" bytes.");
			return fileBytes;
		}finally{
			if(fileIn!=null) fileIn.close();
		}
	}
	
	public static void overwrite(byte data[], File file) throws IOException{
		write(data, file, false);
	}
	
	public static void append(byte data[], File file) throws IOException{
		write(data, file, true);
	}
	
	/** Creates dirs under file if not exist already, unless names of those dirs already exist as files */
	protected static void write(byte data[], File file, boolean append) throws IOException{
		file.getParentFile().mkdirs();
		OutputStream fileOut = null;
		try{
			if(!file.exists()){
				File parent = file.getParentFile();
				if(parent!=null) parent.mkdirs();
				file.createNewFile();
			}
			fileOut = new FileOutputStream(file);
			fileOut.write(data, 0, data.length);
			fileOut.flush();
		}finally{
			if(fileOut!=null) fileOut.close();
		}
	}
	
	public static final File dirWhereThisProgramStarted =
		new File(System.getProperty("user.dir")).getAbsoluteFile();

}
