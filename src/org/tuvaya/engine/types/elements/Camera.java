package org.tuvaya.engine.types.elements;

import java.awt.image.BufferedImage;

import org.tuvaya.engine.gameobjects.Element;
import org.tuvaya.engine.gameobjects.Scene;
import org.tuvaya.engine.tags.Render3DTag;
import org.tuvaya.engine.types.Point3D;

public class Camera extends Object3D {
	
	public BufferedImage render;
	int width, height;
	Scene scene;
	public Runnable ondeath;
	
	public Camera(Point3D pos, int width, int height, Scene scene) {
		super("", pos);
		this.width = width;
		this.height = height;
		this.scene = scene;
		setRenderTag();
	}

	public void setRenderTag() {
		if (getTag(Render3DTag.class) != null) renderTags.remove(0);
		addRenderTag(new Render3DTag(width,height,this,scene));
	}
	
	
	
}
