package org.tuvaya.engine.types.elements;

import java.awt.image.BufferedImage;

import org.tuvaya.engine.gameobjects.Element;
import org.tuvaya.engine.resources.Loader;
import org.tuvaya.engine.types.Point3D;

public class Sprite3D extends Element {
	
	public Point3D pos;
	public BufferedImage texture;
	public float scale;
	
	public Sprite3D(String path, Point3D pos) {
		this.texture = Loader.loadImage(path);
		this.pos = pos;
	}
	
}
