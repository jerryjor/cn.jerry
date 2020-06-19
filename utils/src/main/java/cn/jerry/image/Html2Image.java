package cn.jerry.image;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Html2Image {

    private Html2Image() {
        super();
    }

    public static Font createFont(String srcFile) throws IOException, FontFormatException {
        try (InputStream stream = Html2Image.class.getResourceAsStream(srcFile)) {
            return Font.createFont(0, stream);
        }
    }

    public static String readHtml(String srcFile) throws IOException {
        byte[] bytes;
        try (InputStream stream = Html2Image.class.getResourceAsStream(srcFile);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024 * 1024];
            int length;
            while ((length = stream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }
            baos.flush();
            bytes = baos.toByteArray();
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /**
     * html转换为png文件
     *
     * @param html html的文本信息
     * @param font 字体
     * @return 图片文件字节码
     */
    public static byte[] html2png(String html, Font font) throws IOException {
        JTextPane tp = new JTextPane();
        EmptyBorder eb = new EmptyBorder(0, 50, 0, 50);
        tp.setBorder(eb);
        tp.setContentType("text/html");
        tp.setText(html);
        tp.setFont(font);
        Dimension d = tp.getUI().getPreferredSize(tp);
        int height = d.height + 20;
        int width = d.width + 15;
        tp.setSize(width, height);
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.setClip(0, 0, width, height);
        paintPage(g, tp);
        g.dispose();
        byte[] bytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            baos.flush();
            bytes = baos.toByteArray();
        }
        return bytes;
    }

    /**
     * 画页面的方法
     *
     * @param g     画笔
     * @param panel 画板
     */
    public static void paintPage(Graphics g, JTextPane panel) {
        Graphics2D g2 = (Graphics2D) g;
        g2.translate(0f, 0f);
        panel.paint(g2);
    }

}
