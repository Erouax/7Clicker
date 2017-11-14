package com.ruffian7.sevenclicker;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.ImageIcon;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

import com.ruffian7.sevenclicker.gui.ClickerGui;
import com.ruffian7.sevenclicker.listener.KeyListener;
import com.ruffian7.sevenclicker.listener.MouseListener;

public class AutoClicker implements Runnable {
	
	private static final Random RANDOM = new Random();

	public final ClickerGui gui;
	
	private final ImageIcon buttonOff;
	private final ImageIcon buttonOn;
	public final Robot robot;
	
	public Point mousePos;

	public boolean activated;
	public boolean blockHit;
	public boolean skipNext;
	public boolean toggled;

	private int delay = -1;
	public long lastTime = 0;
	public int minCPS = 8;
	public int maxCPS = 12;
	public int button = 1;

	public String[] toggleKey = { "", "" };
	public int toggleMouseButton = 3;
	
	public AutoClicker() {
		this.gui = new ClickerGui();
		
		this.buttonOn = new ImageIcon(this.getClass().getClassLoader().getResource("assets/power_button_on.png"));
		this.buttonOff = new ImageIcon(this.getClass().getClassLoader().getResource("assets/power_button.png"));
		
		Robot robot;
		try {
			robot = new Robot();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.robot = robot;
	}
	
	public static void main(String[] args) {
		LogManager.getLogManager().reset();
		Logger.getLogger(GlobalScreen.class.getPackage().getName()).setLevel(Level.OFF);

		try {
			GlobalScreen.registerNativeHook();
			GlobalScreen.addNativeMouseListener(new MouseListener());
			GlobalScreen.addNativeKeyListener(new KeyListener());
		} catch (NativeHookException | AWTException e) {
			throw new RuntimeException(e);
		}
		
		new Thread(new AutoClicker()).start();
	}
	
	public void run() {
		try {
			while (true) {
				Thread.sleep(1);
				
				if (this.delay == -1) {
					this.randomiseDelay();
				}
				
				if (this.activated && this.toggled && !this.gui.focused) {
					if (System.currentTimeMillis() - this.lastTime >= this.delay) {
						this.lastTime = System.currentTimeMillis();
						
						this.click();
						
						this.randomiseDelay();
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private int click() {
		this.skipNext = true;
		
		this.robot.mousePress((this.button == 1) ? 16 : 4);
		
		int sleep = this.sleepRandom();
		
		this.robot.mouseRelease((this.button == 1) ? 16 : 4);

		if (this.blockHit) {
			sleep += this.sleepRandom();
			
			this.robot.mousePress((button == 1) ? 4 : 16);
			
			sleep += this.sleepRandom();
			
			this.robot.mouseRelease((button == 1) ? 4 : 16);
		}
		
		return sleep;
	}
	
	public void toggle() {
		if (this.toggled) {
			this.gui.powerButton.setIcon(this.buttonOff);
		} else {
			this.gui.powerButton.setIcon(this.buttonOn);
		}
		
		this.toggled = !this.toggled;

		this.activated = false;
		this.skipNext = false;
		this.blockHit = false;
	}

	private int sleepRandom() {
		int sleep = RANDOM.nextInt(35) + 5;
		try {
			Thread.sleep(sleep);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		return sleep;
	}

	private void randomiseDelay() {
		this.delay = RANDOM.nextInt((1000 / this.minCPS) - (1000 / this.maxCPS) + 1) + (1000 / this.maxCPS);
	}

}
