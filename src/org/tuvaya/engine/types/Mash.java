package org.tuvaya.engine.types;

public class Mash {
	
	public Point3D[] vertices;
	public Point3D[] normales;
	public Point3D[] textures;
	public Triangle[] triangres;
	
	public Mash(Point3D[] ve, Point3D[] te, Point3D[] no, Triangle[] tr) {
		vertices = ve;
		textures = te;
		normales = no;
		triangres = tr;
		
	}
	
}
