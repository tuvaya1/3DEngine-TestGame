package org.tuvaya.test;

import org.tuvaya.engine.gameobjects.Scene;
import org.tuvaya.engine.gameobjects.Tag;
import org.tuvaya.engine.tags.Console;
import org.tuvaya.engine.tags.LinkerTag;
import org.tuvaya.engine.tags.NoCollision;
import org.tuvaya.engine.types.Point3D;
import org.tuvaya.engine.types.Triangle;
import org.tuvaya.engine.types.elements.Object3D;
import org.tuvaya.engine.types.elements.Sprite3D;

public class Bullet extends Object3D {

	Scene scene;
	float speed;
	int time;
	
	static float r = 0.1f;
	
	String name;
	String from;
	
	public Bullet(String name, Scene sc, Point3D p, Point3D d, float s, String from) {
		super("assets/models/bullet.obj", p);
		this.scale = 0.01f;
		this.name = name;
		this.from = from;
		this.scene = sc;
		this.dir = d;
		
		this.speed = s;
		addTag(new NoCollision());
		addTag(()->{
			time++;
			Object3D cam = scene.get("camera", Object3D.class);
			Console console = cam.getTag(Console.class);
			
			Point3D dn = new Point3D(0,0,1).rotate(dir);
			//console.chat.add(dir + " " + dn);
			Point3D L = pos.sub(cam.pos);
			float a = dn.dot(dn);
			float b = 2*L.dot(dn);
			float c = L.dot(L)-r*r;
			
			float D = b*b-4*a*c;
			
			
			float t1 = (float) ((-b+Math.sqrt(D))/(2*a));
			float t2 = (float) ((-b-Math.sqrt(D))/(2*a));
			float t = Math.min(Math.max(0,t1), Math.max(0,t2));
			if (t > 0 && t <= r) {
				console.processInput("~kill " + Console.name, true);
				console.processInput("~getw -", true);
				console.processInput("~add " + from + " kills", false);
				console.processInput("~add " + Console.name + " deaths", false);
				scene.elements.remove(name);
			}else {
				final String n = "step" + System.currentTimeMillis();
				scene.add(n, new Sprite3D("assets/sprites/smoke.png", pos))
				.addTag(new Tag() {
					int time = 50;
					String name = n;
					@Override
					public void action() throws Exception {
						time--;
						if (time<0) {
							scene.elements.remove(name);
						}
					}})
				;
				scene.get(n, Sprite3D.class).scale = 0.005f;
				pos = pos.add(dn.mul(speed));
				Point3D co = new Point3D(0,0,0);
				if (scene.get("linker").getTag(LinkerTag.class).collision.mash != null)
					co = checkCollision(0.01f, scene.get("linker").getTag(LinkerTag.class).collision);
				if (co.sqrt() > 0) {
					scene.elements.remove(name);
				}
			}
			if (time > 50*1/speed) scene.elements.remove(name);
		});
	}
	
	public Point3D checkCollision(float r, Object3D linker) {
			Point3D spos = new Point3D(pos);
			Point3D pos = new Point3D(spos);
			
			for (Triangle t : linker.mash.triangres) {
				Point3D a = linker.mash.vertices[(int)t.v0];
		        Point3D b = linker.mash.vertices[(int)t.v1];
		        Point3D c = linker.mash.vertices[(int)t.v2];
				float cx = (a.x + b.x + c.x) / 3f;
		        float cy = (a.y + b.y + c.y) / 3f;
		        float cz = (a.z + b.z + c.z) / 3f;
		        
		        // Расстояние от вершины до центра (максимум)
		        float dxa = a.x - cx, dya = a.y - cy, dza = a.z - cz;
		        float dxb = b.x - cx, dyb = b.y - cy, dzb = b.z - cz;
		        float dxc = c.x - cx, dyc = c.y - cy, dzc = c.z - cz;
		        float triRadiusSq = Math.max(
		            Math.max(dxa*dxa + dya*dya + dza*dza, dxb*dxb + dyb*dyb + dzb*dzb),
		            dxc*dxc + dyc*dyc + dzc*dzc
		        );
		        float triRadius = (float)Math.sqrt(triRadiusSq);
		        
		        // Расстояние от сферы до центра треугольника
		        float ddx = pos.x - cx, ddy = pos.y - cy, ddz = pos.z - cz;
		        float distToCenterSq = ddx*ddx + ddy*ddy + ddz*ddz;
		        float maxDist = r + triRadius;
		        
		        // Если сфера далеко от треугольника — пропускаем
		        if (distToCenterSq > maxDist * maxDist) continue;
		        
		        // ==== Находим ближайшую точку на треугольнике ====
		        Point3D closest = closestPointOnTriangle(pos.x, pos.y, pos.z, a, b, c);
		        
		        // Вектор от ближайшей точки к центру сферы
		        float vx = pos.x - closest.x;
		        float vy = pos.y - closest.y;
		        float vz = pos.z - closest.z;
		        float distSq = vx*vx + vy*vy + vz*vz;
		        
		        // Если сфера пересекает треугольник
		        if (distSq < r * r && distSq > 1e-8f) {
		            float dist = (float)Math.sqrt(distSq);
		            float penetration = r - dist;
		            
		            // Нормализуем вектор направления
		            float nx = vx / dist;
		            float ny = vy / dist;
		            float nz = vz / dist;
		            
		            // Вытесняем сферу
		            pos.x += nx * penetration;
		            pos.y += ny * penetration;
		            pos.z += nz * penetration;
		        }
			}
			return pos.sub(spos);
		}
		
		private Point3D closestPointOnTriangle(float px, float py, float pz, Point3D a, Point3D b, Point3D c) {
		    // Вектор ребра AB и AC
		    float abx = b.x - a.x, aby = b.y - a.y, abz = b.z - a.z;
		    float acx = c.x - a.x, acy = c.y - a.y, acz = c.z - a.z;
		    float apx = px - a.x, apy = py - a.y, apz = pz - a.z;
		    
		    float d1 = abx*apx + aby*apy + abz*apz;
		    float d2 = acx*apx + acy*apy + acz*apz;
		    
		    // Ближайшая точка = A (вершина A)
		    if (d1 <= 0 && d2 <= 0) return a;
		    
		    // Проверка вершины B
		    float bpx = px - b.x, bpy = py - b.y, bpz = pz - b.z;
		    float d3 = abx*bpx + aby*bpy + abz*bpz;
		    float d4 = acx*bpx + acy*bpy + acz*bpz;
		    if (d3 >= 0 && d4 <= d3) return b;
		    
		    // Проверка ребра AB
		    float vc = d1*d4 - d3*d2;
		    if (vc <= 0 && d1 >= 0 && d3 <= 0) {
		        float v = d1 / (d1 - d3);
		        return new Point3D(a.x + abx*v, a.y + aby*v, a.z + abz*v);
		    }
		    
		    // Проверка вершины C
		    float cpx = px - c.x, cpy = py - c.y, cpz = pz - c.z;
		    float d5 = abx*cpx + aby*cpy + abz*cpz;
		    float d6 = acx*cpx + acy*cpy + acz*cpz;
		    if (d6 >= 0 && d5 <= d6) return c;
		    
		    // Проверка ребра AC
		    float vb = d5*d2 - d1*d6;
		    if (vb <= 0 && d2 >= 0 && d6 <= 0) {
		        float w = d2 / (d2 - d6);
		        return new Point3D(a.x + acx*w, a.y + acy*w, a.z + acz*w);
		    }
		    
		    // Проверка ребра BC
		    float va = d3*d6 - d5*d4;
		    if (va <= 0 && (d4 - d3) >= 0 && (d5 - d6) >= 0) {
		        float w = (d4 - d3) / ((d4 - d3) + (d5 - d6));
		        return new Point3D(
		            b.x + (c.x - b.x) * w,
		            b.y + (c.y - b.y) * w,
		            b.z + (c.z - b.z) * w
		        );
		    }
		    
		    // Точка внутри треугольника (проекция на плоскость)
		    float denom = 1f / (va + vb + vc);
		    float v = vb * denom;
		    float w = vc * denom;
		    return new Point3D(
		        a.x + abx*v + acx*w,
		        a.y + aby*v + acy*w,
		        a.z + abz*v + acz*w
		    );
		}
	
}
