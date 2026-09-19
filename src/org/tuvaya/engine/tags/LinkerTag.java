package org.tuvaya.engine.tags;

import java.util.ArrayList;

import org.tuvaya.engine.gameobjects.Scene;
import org.tuvaya.engine.gameobjects.Tag;
import org.tuvaya.engine.types.Mash;
import org.tuvaya.engine.types.Point3D;
import org.tuvaya.engine.types.Triangle;
import org.tuvaya.engine.types.elements.Object3D;

public class LinkerTag implements Tag {

	public Object3D render;
	public Object3D collision;
	Scene scene;
	public LinkerTag(Object3D linker, Scene scene) {
		this.render = new Object3D("",new Point3D(0,0,0));
		this.collision = new Object3D("",new Point3D(0,0,0));
		this.scene = scene;
		
	}
	
	@Override
	public void action() throws Exception {
		link(render, NoRenderer.class);
		link(collision, NoCollision.class);
	}
	
	private void link(Object3D object, Class<? extends Tag> except) {
	    
	    ArrayList<Point3D> v = new ArrayList<>();
	    ArrayList<Point3D> vt = new ArrayList<>();
	    ArrayList<Point3D> vn = new ArrayList<>();
	    ArrayList<Triangle> tr = new ArrayList<>();
	    
	    // Используем массивы для обхода ограничения "effectively final"
	    int[] vis = {0};
	    int[] vtis = {0};
	    int[] vnis = {0};
	    
	    scene.forEach(Object3D.class, (obj)->{
	    	Mash mash = obj.getMash();
	        if (mash == null || obj.getTag(except) != null) return;
	        
	        // Добавляем все вершины
	        for (Point3D p : mash.vertices) {
	            v.add(p.mul(obj.scale).rotate(obj.dir).add(obj.pos));
	        }
	        // Добавляем все текстурные координаты
	        for (Point3D p : mash.textures) {
	            vt.add(p);
	        }
	        // Добавляем все нормали
	        for (Point3D p : mash.normales) {
	            vn.add(p.rotate(obj.dir));
	        }
	        
	        // Добавляем треугольники со смещением индексов
	        for (Triangle tri : mash.triangres) {
	            Triangle t = new Triangle(tri);
	            t.add(vis[0], vtis[0], vnis[0]);
	            //t.color = obj.color;
	            tr.add(t);
	        }
	        
	        // Обновляем смещения для следующего объекта
	        vis[0] += mash.vertices.length;
	        vtis[0] += mash.textures.length;   // ВАЖНО: textures.length, не vertices.length!
	        vnis[0] += mash.normales.length;   // ВАЖНО: normales.length, не vertices.length!
	    });
	    
	    object.mash = new Mash(
	        v.toArray(new Point3D[0]),
	        vt.toArray(new Point3D[0]),
	        vn.toArray(new Point3D[0]),
	        tr.toArray(new Triangle[0])
	    );
	    
	}
	
}
