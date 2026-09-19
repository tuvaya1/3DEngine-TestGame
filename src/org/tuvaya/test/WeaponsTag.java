package org.tuvaya.test;

import org.tuvaya.engine.gameobjects.Tag;
import org.tuvaya.engine.types.Point3D;
import org.tuvaya.engine.types.elements.Object3D;

public class WeaponsTag implements Tag {

	Object3D weapon;
	Object3D camera;

	public WeaponsTag(Object3D object3d, Object3D camera) {
		weapon = object3d;
		this.camera = camera;
	}
	
	float time;
	
	@Override
	public void action() throws Exception {
		if (WeaponUpdateTag.weapons[WeaponUpdateTag.current] != null)
			weapon.mash = WeaponUpdateTag.weapons[WeaponUpdateTag.current].mash;
		else
			weapon.mash = null;
		
		weapon.pos = camera.pos.add(new Point3D(-0.3f,-0.2f,-0.11f).rotate(camera.dir));
		weapon.dir = camera.dir.add((WeaponUpdateTag.weapons[WeaponUpdateTag.current] != null && !WeaponUpdateTag.weapons[WeaponUpdateTag.current].can)
				? new Point3D((time*360),0,0): new Point3D(0,0,0));
		time+=0.1f;
	}

}
