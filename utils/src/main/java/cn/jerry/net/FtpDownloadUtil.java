package cn.jerry.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class FtpDownloadUtil {
    private static final String CHARSET_LOCAL = "GBK";

    /**
     * 下载文件
     *
     * @param host     服务器
     * @param port     端口
     * @param user     用户名
     * @param pwd      密码
     * @param baseDir  文件所在目录
     * @param fileName 文件名
     * @param charset  文件名编码
     * @return
     * @throws IOException
     */
    public static byte[] download(String host, Integer port, String user, String pwd, String baseDir,
            String fileName, String charset) throws IOException {

        FTPFile needFile = null;
        FTPClient client = connect(host, port, user, pwd);

        client.changeWorkingDirectory(baseDir);
        FTPFile[] files = client.listFiles();
        for (FTPFile file : files) {
            try {
                if (new String(file.getName().getBytes(charset), CHARSET_LOCAL).equals(fileName)) {
                    needFile = file;
                    break;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        byte[] fileContent = null;
        if (needFile != null && needFile.isFile()) {
            ByteArrayOutputStream outputStream = null;
            try {
                outputStream = new ByteArrayOutputStream();
                client.retrieveFile(needFile.getName(), outputStream);
                outputStream.flush();
                fileContent = outputStream.toByteArray();
            } catch (IOException e) {
                throw e;
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        close(client);
        return fileContent;
    }

    /**
     * 连接服务器
     *
     * @param host
     * @param port
     * @param user
     * @param pwd
     * @return
     * @throws IOException
     */
    private static FTPClient connect(String host, Integer port, String user, String pwd) throws IOException {
        FTPClient client = new FTPClient();
        if (port != null) {
            client.connect(host, port);
        } else {
            client.connect(host);
        }
        if (user != null && !user.isEmpty()) {
            client.login(user, pwd);
        }
        client.setFileType(FTPClient.BINARY_FILE_TYPE);
        client.enterLocalPassiveMode();
        return client;
    }

    /**
     * 关闭ftpClient链接
     */
    private static void close(FTPClient client) {
        if (client != null && client.isConnected()) {
            try {
                client.logout();
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
