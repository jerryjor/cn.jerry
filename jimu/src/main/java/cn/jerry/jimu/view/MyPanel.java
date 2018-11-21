package cn.jerry.jimu.view;

import java.awt.Color;

import javax.swing.JPanel;

public class MyPanel extends JPanel {
	private static final long serialVersionUID = -7243307847387569327L;

	public MyPanel() {
		super();
		init();
	}

	private void init() {
		this.setFocusable(false);
		this.setBackground(null);
		this.setOpaque(false);
	}

	/**
	 * 设置背景色，同时设置为不透明
	 * 
	 * @history 面板设置为透明模式，from ver 2.2.1
	 */
	public void setPanelBg(Color c) {
		setBackground(c);
		setOpaque(true);
		repaint();
	}

	/**
	 * 恢复面板到默认：无背景色，透明
	 * 
	 * @history 面板设置为透明模式，from ver 2.2.1
	 */
	public void resetPanel2Default() {
		setBackground(null);
		setOpaque(false);
		repaint();
	}
}
