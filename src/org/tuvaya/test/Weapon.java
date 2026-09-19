package org.tuvaya.test;

import org.tuvaya.engine.tags.Console;
import org.tuvaya.engine.types.Mash;
import org.tuvaya.engine.types.Point3D;
import org.tuvaya.engine.types.elements.Object3D;

public class Weapon {
	
	public static final Weapon pistol = new Weapon("pistol", 2.0f, 0.1f, 300);
	public static final Weapon revolver = new Weapon("revolver", 0f, 1.0f, 400);
	public static final Weapon ak47 = new Weapon("ak47", 10.0f, 0.5f, 50);
	public static final Weapon rifle = new Weapon("rifle", 0f, 2.5f, 500);
	public static final Weapon minigun = new Weapon("minigun", 50.0f, 2.0f, 0);
	
	public float random;
	public float speed;
	public Mash mash;
	public int time;
	
	public Weapon(String name, float random, float speed, int time) {
		
		mash = new Object3D("assets/models/weapon/"+name+".obj", new Point3D(0,0,0)).mash;
		this.random = random;
		this.speed = speed;
		this.time = time;
		
		
	}
	
	public boolean can = true;
	
	public void shoot(Object3D camera) {
		if (can) {
			Point3D p = camera.pos.add(new Point3D(0,0,-1).rotate(camera.dir).mul(0.2f));
			Point3D d = camera.dir.add(new Point3D(0,180,0)).mul(new Point3D(-1,1,1)).add(new Point3D((float)(Math.random()-0.5)*random,(float)(Math.random()-0.5)*random,0));
			camera.getTag(Console.class).processInput("~shoot " + p.x + " " + p.y + " " + p.z + " " + d.x + " " + d.y + " " + d.z + " " + speed + " " + Console.name, false);
			new Thread(()->{
				can = false;
				try {
					Thread.sleep(time);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				can = true;
			}).start();
		}
	}
	
}
