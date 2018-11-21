package cn.jerry.jimu.view;

import cn.jerry.jimu.constant.ViewProp;

import javax.swing.*;
import java.awt.*;

public class MyLabel extends JLabel {
	private static final long serialVersionUID = 2008097875284355892L;

	public MyLabel() {
		super();
		init();
	}

	public MyLabel(String text) {
		this();
		this.setText(text);
	}

	public MyLabel(Font f) {
		this();
		this.setFont(f);
	}

	public MyLabel(String text, Font f) {
		this();
		this.setText(text);
		this.setFont(f);
	}

	public MyLabel(ImageIcon ii) {
		super(ii);
	}

	private void init() {
		this.setFocusable(false);
		this.setForeground(ViewProp.FONT_COLOR);
		this.setAlignmentX(CENTER_ALIGNMENT);
		this.setAlignmentY(CENTER_ALIGNMENT);
		this.setHorizontalAlignment(CENTER);
		this.setVerticalAlignment(CENTER);
		this.setHorizontalTextPosition(CENTER);
		this.setVerticalTextPosition(CENTER);
	}
}
