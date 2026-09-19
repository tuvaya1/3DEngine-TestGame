package org.tuvaya.test;

import org.tuvaya.engine.gameobjects.Tag;

public class PlayerTag implements Tag {
	
	public String name;
	public int deaths;
	public int kills;
	public int flags;
	public float besttime;
	public static float time;
	
	@Override
	public void action() throws Exception {
		time-= 1/60f;
	}

}
