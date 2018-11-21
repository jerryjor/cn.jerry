package cn.jerry.jimu.view;

import javax.swing.JFrame;

public class MyFrame extends JFrame {
	private static final long serialVersionUID = 7076270908463003995L;

	public MyFrame() {
		super();
		init();
	}

	public MyFrame(String title) {
		super(title);
		init();
	}

	private void init() {
		this.setFocusable(true);
		this.setResizable(false);
		this.setDefaultCloseOperation(MyFrame.EXIT_ON_CLOSE);
	}
}
