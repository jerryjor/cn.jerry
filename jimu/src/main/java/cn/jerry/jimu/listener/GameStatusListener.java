package cn.jerry.jimu.listener;

import cn.jerry.jimu.controller.DataController;
import cn.jerry.jimu.controller.ViewController;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class GameStatusListener implements MouseListener {

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// do nothing
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// do nothing
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// do nothing
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// do nothing
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			// 开始/暂停/继续
			DataController.giveOrderByStatus();
			ViewController.refreshStartButtonText();
			break;
		}
	}

}
