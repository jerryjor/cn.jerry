package cn.jerry.jimu.listener;

import cn.jerry.jimu.constant.GameOrder;
import cn.jerry.jimu.controller.DataController;
import cn.jerry.jimu.controller.ViewController;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyPressedListener implements KeyListener {

	@Override
	public void keyPressed(KeyEvent e) {
		if (!DataController.isStart()) {
			switch (e.getKeyCode()) {
			case KeyEvent.VK_UP:
				// 速度等级+
				ViewController.changeSpeedLevel(true);
				break;
			case KeyEvent.VK_DOWN:
				// 速度等级-
				ViewController.changeSpeedLevel(false);
				break;
			case KeyEvent.VK_M:
				// 改变方块模式
				DataController.changeBlockMode();
				ViewController.changeBlockMode();
				break;
			case KeyEvent.VK_I:
				// 设置是否自动增长
				DataController.changeAutoIncrease();
				ViewController.changeAutoIncrease();
				break;
			}
		} else {
			switch (e.getKeyCode()) {

			case KeyEvent.VK_UP:
				// 旋转方块
				DataController.giveOrder(GameOrder.ROTATE, true);
				break;
			case KeyEvent.VK_DOWN:
				// 直接掉落方块
				DataController.giveOrder(GameOrder.DROP, true);
				break;
			case KeyEvent.VK_LEFT:
				// 左移方块
				DataController.giveOrder(GameOrder.MOVE_LEFT, true);
				break;
			case KeyEvent.VK_RIGHT:
				// 右移方块
				DataController.giveOrder(GameOrder.MOVE_RIGHT, true);
				break;
			}
		}

		switch (e.getKeyCode()) {
		case KeyEvent.VK_S:
			// 切换阴影显示
			DataController.changeShadowShowing();
			ViewController.changeShadowShowing();
			break;
		case KeyEvent.VK_SPACE:
		case KeyEvent.VK_ENTER:
			// 开始/暂停/继续
			DataController.giveOrderByStatus();
			ViewController.refreshStartButtonText();
			break;
		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// do nothing
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// do nothing
	}

}
