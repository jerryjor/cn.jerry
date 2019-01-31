package cn.jerry.fabric.api.fabric.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.web.multipart.MultipartFile;

public class FileUtil {

	    public static String unZipFiles(String unZipRealPath,MultipartFile multipartFile)throws IOException{
	        //如果保存解压缩文件的目录不存在，则进行创建，并且解压缩后的文件总是放在以fileName命名的文件夹下
	        File unZipFile = new File(unZipRealPath);
	        if (!unZipFile.exists()) {
	            unZipFile.mkdirs();
	        }
	        //ZipInputStream用来读取压缩文件的输入流
	        ZipInputStream zipInputStream = new ZipInputStream(multipartFile.getInputStream());
	        //压缩文档中每一个项为一个zipEntry对象，可以通过getNextEntry方法获得，zipEntry可以是文件，也可以是路径，比如abc/test/路径下
	        ZipEntry zipEntry;
	        try {
	            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
	                String zipEntryName = zipEntry.getName();
	                //将目录中的1个或者多个\置换为/，因为在windows目录下，以\或者\\为文件目录分隔符，linux却是/
	                String outPath = (unZipRealPath + zipEntryName).replaceAll("\\+", "/");
	                //判断所要添加的文件所在路径或者
	                // 所要添加的路径是否存在,不存在则创建文件路径
	                File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
	                if (!file.exists()) {
	                    file.mkdirs();
	                }
	                //判断文件全路径是否为文件夹,如果是,在上面三行已经创建,不需要解压
	                if (new File(outPath).isDirectory()) {
	                    continue;
	                }

	                OutputStream outputStream = new FileOutputStream(outPath);
	                byte[] bytes = new byte[4096];
	                int len;
	                //当read的返回值为-1，表示碰到当前项的结尾，而不是碰到zip文件的末尾
	                while ((len = zipInputStream.read(bytes)) > 0) {
	                    outputStream.write(bytes, 0, len);
	                }
	                outputStream.close();
	                //必须调用closeEntry()方法来读入下一项
	                zipInputStream.closeEntry();
	            }
	            zipInputStream.close();
	            System.out.println("******************解压完毕********************");
	            return unZipRealPath;
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	        return unZipRealPath;
	    }
	    
	    
	    public static void delFolder(String folderPath) {
	        try {
	           delAllFile(folderPath); //删除完里面所有内容
	           String filePath = folderPath;
	           filePath = filePath.toString();
	           java.io.File myFilePath = new java.io.File(filePath);
	           myFilePath.delete(); //删除空文件夹
	        } catch (Exception e) {
	          e.printStackTrace(); 
	        }
	   }
	    
	    public static boolean delAllFile(String path) {
	        boolean flag = false;
	        File file = new File(path);
	        if (!file.exists()) {
	          return flag;
	        }
	        if (!file.isDirectory()) {
	          return flag;
	        }
	        String[] tempList = file.list();
	        File temp = null;
	        for (int i = 0; i < tempList.length; i++) {
	           if (path.endsWith(File.separator)) {
	              temp = new File(path + tempList[i]);
	           } else {
	               temp = new File(path + File.separator + tempList[i]);
	           }
	           if (temp.isFile()) {
	              temp.delete();
	           }
	           if (temp.isDirectory()) {
	              delAllFile(path + "/" + tempList[i]);//先删除文件夹里面的文件
	              delFolder(path + "/" + tempList[i]);//再删除空文件夹
	              flag = true;
	           }
	        }
	        return flag;
	      }
}
