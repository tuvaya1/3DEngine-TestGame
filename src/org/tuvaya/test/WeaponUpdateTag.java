package org.tuvaya.test;

import java.awt.event.KeyEvent;

import org.tuvaya.engine.gameobjects.Tag;
import org.tuvaya.engine.tags.Console;
import org.tuvaya.engine.types.elements.Object3D;
import org.tuvaya.engine.window.inputs.Keyboard;
import org.tuvaya.engine.window.inputs.Mouse;

public class WeaponUpdateTag implements Tag {
	
	public static Weapon[] weapons = {
			null,
			null,
			null,
			null,
			null,
	};
	
	public static int current = 0;
	
	Object3D camera;
	Mouse minput;
	Keyboard input;
	
	public WeaponUpdateTag(Object3D camera, Mouse minput, Keyboard input) {
		this.camera = camera;
		this.minput = minput;
		this.input = input;
	}
	
	@Override
	public void action() throws Exception {
		if (Console.inchat) return;
		if (input.isKeyPressed(KeyEvent.VK_1)) current = 0;
		if (input.isKeyPressed(KeyEvent.VK_2)) current = 1;
		if (input.isKeyPressed(KeyEvent.VK_3)) current = 2;
		if (input.isKeyPressed(KeyEvent.VK_4)) current = 3;
		if (input.isKeyPressed(KeyEvent.VK_5)) current = 4;
		
		
		if (minput.getButtonPressed() == 1) {
			if (weapons[current] != null) weapons[current].shoot(camera);
		}
	}

}
