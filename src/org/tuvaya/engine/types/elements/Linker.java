package org.tuvaya.engine.types.elements;

import org.tuvaya.engine.gameobjects.Scene;
import org.tuvaya.engine.tags.LinkerTag;
import org.tuvaya.engine.tags.NoRenderer;
import org.tuvaya.engine.types.Point3D;

public class Linker extends Object3D {

	public Linker(Scene scene) {
		super("", new Point3D(0,0,0));
		addTag(new NoRenderer());
		addRenderTag(new LinkerTag(this, scene));
	}

}
