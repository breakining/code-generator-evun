package DateBaseTools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class SaveFile {
	
	public static void writeFile(String path,String context) throws Exception{
		File f =new File(path); 
		OutputStreamWriter write = null;
		if (!f.isDirectory()) {
			f.mkdirs();
		}
		if (f.exists()) {
			f.delete();
		}
		f.createNewFile();
		write = new OutputStreamWriter(new FileOutputStream(f),"UTF-8");
	   
		try {
			write.write(context);
			write.flush();
			System.out.println(path+" 写入成功!");
		} catch (IOException e) {
			throw new Exception(e.getMessage());
		}finally{
			if (write!=null) {
				write.close();
			}
		}
	}
	
}
