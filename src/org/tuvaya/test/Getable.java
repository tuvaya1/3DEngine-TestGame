package org.tuvaya.test;

import org.tuvaya.engine.gameobjects.Tag;
import org.tuvaya.engine.tags.NoCollision;
import org.tuvaya.engine.tags.NoRenderer;
import org.tuvaya.engine.types.Point3D;
import org.tuvaya.engine.types.elements.Object3D;

public class Getable implements Tag {

	Object3D s;
	Object3D p;
	Runnable action;
	
	public Getable(Object3D s, Object3D camera, Runnable object) {
		this.s = s;
		s.addTag(new NoCollision());
		p = camera;
		action = object;
	}

	boolean can = true;
	
	@Override
	public void action() throws Exception {
		if (can && s.pos.sub(p.pos).sqrt() <= 0.3f) {
			action.run();
			new Thread(()->{
				s.addTag(new NoRenderer());
				can = false;
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				can = true;
				s.updateTags.remove(2);
			})
			.start();
		}
		s.dir = s.dir.add(new Point3D(0,5,0));
		s.scale = 0.1f;
	}

}
