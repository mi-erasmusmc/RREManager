package org.erasmusmc.rremanager.gui;

import javax.swing.JPanel;

import org.erasmusmc.rremanager.RREManager;

public class MainFrameTab extends JPanel {
	private static final long serialVersionUID = -6443432062427097014L;
	
	
	protected RREManager rreManager;
	protected MainFrame mainFrame;
	

	public MainFrameTab(RREManager rreManager, MainFrame mainFrame) {
		super();
		this.rreManager = rreManager;
		this.mainFrame = mainFrame;
	}
}
