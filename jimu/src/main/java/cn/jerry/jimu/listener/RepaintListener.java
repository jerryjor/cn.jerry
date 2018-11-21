package cn.jerry.jimu.listener;

import cn.jerry.jimu.view.DrawingView;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class RepaintListener implements WindowListener {
    private DrawingView view;

    public RepaintListener(DrawingView view) {
        this.view = view;
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
        this.view.redrawCachePanel();
        this.view.redrawMainPanel();
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

}
