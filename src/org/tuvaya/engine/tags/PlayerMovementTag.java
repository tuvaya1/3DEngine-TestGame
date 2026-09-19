package org.tuvaya.engine.tags;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import org.tuvaya.engine.gameobjects.Scene;
import org.tuvaya.engine.gameobjects.Tag;
import org.tuvaya.engine.types.Point3D;
import org.tuvaya.engine.types.Triangle;
import org.tuvaya.engine.types.elements.Object3D;
import org.tuvaya.engine.window.inputs.Keyboard;
import org.tuvaya.engine.window.inputs.Mouse;

public class PlayerMovementTag implements Tag {
	
	Object3D p;
	Console console;
	Object3D linker;
	Keyboard input;
	Mouse minput;
	Robot robot;
	
	int wx;
	int wy;
	
	public PlayerMovementTag(Object3D player, Keyboard input, Mouse minput, Point l, Object3D linker) {
		this.p = player;
		console = p.getTag(Console.class);
		this.input = input;
		this.minput = minput;
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		this.wx = l.x+100;
		this.wy = l.y+100;
		this.linker = linker.getTag(LinkerTag.class).collision;
		
	}

	float speed = 0.02f;
	static Point3D s = new Point3D(0,0,0);
	Point3D g = new Point3D(0,-0.008f,0);
	boolean m;
	boolean n;
	float sensity = 0.05f; // Нормальная чувствительность мыши
	
	int lx,ly;
	
	@Override
	public void action() throws Exception {
		
		
		//спец клавиша
		/*if (input.isKeyPressed(KeyEvent.VK_L)) {
			if (m) {
				Render3DTag.mode = 1 - Render3DTag.mode;
			}
			m = false;
		} else {
			m = true;
		}*/
		//инпут
		
		Point3D d = new Point3D(0,0,0);
		Point3D r = new Point3D(0,0,0);
		
		if (!Console.inchat) {
			
			/*if (input.isKeyPressed(KeyEvent.VK_SHIFT)) {
				if (n) {
				System.out.println("new Point3D(" + p.pos.x + ", " + p.pos.y + ", " + p.pos.z + "),");
				}
				n = false;
			}else {
				n = true;
			}*/
			
			d = new Point3D(
					((input.isKeyPressed(KeyEvent.VK_D) ? 1:0)-(input.isKeyPressed(KeyEvent.VK_A) ? 1:0)),
					0,
					((input.isKeyPressed(KeyEvent.VK_W) ? 1:0)-(input.isKeyPressed(KeyEvent.VK_S) ? 1:0))
			);
	
			if (input.isKeyPressed(KeyEvent.VK_Q)) d = new Point3D(-1,0,1);
			if (input.isKeyPressed(KeyEvent.VK_E)) d = new Point3D(1,0,1);
			
			
			if (minput != null) {
				Point3D rm = new Point3D(
						 (minput.getY() - ly)*0.5f, 
						 (minput.getX() - lx)*0.5f,  
						0
				);
				ly = minput.getY();
				lx = minput.getX();
				rm.mul(sensity);
				r = r.add(rm);
			}
			Point3D rk = new Point3D(
					-((input.isKeyPressed(KeyEvent.VK_UP) ? 1:0)-(input.isKeyPressed(KeyEvent.VK_DOWN) ? 1:0)),
					((input.isKeyPressed(KeyEvent.VK_RIGHT) ? 1:0)-(input.isKeyPressed(KeyEvent.VK_LEFT) ? 1:0)),
					0
			);
			r = r.add(rk);
		
		}
		
		
		//оутпут
		d = d.mul(-speed);
		Point3D dir = new Point3D(0,p.dir.y,0);
		Point3D dp = new Point3D(0,0,0);
		dp = dp.add(new Point3D(0,0,1).rotate(dir).mul(d.z)); // Вперед
		dp = dp.add(new Point3D(1,0,0).rotate(dir).mul(d.x)); // Вбок
		dp = dp.add(new Point3D(0,1,0).mul(d.y));              // Вверх/вниз
		if (Render3DTag.delta != 0) dp.mul(1f/Render3DTag.delta);
		s = s.add(dp);
		boolean canjump = false;
		for (int i = 0; i < Math.ceil(s.sqrt()/0.3f); i++) {
			p.pos = p.pos.add(s.mul(1f/(float)Math.ceil(s.sqrt()/0.3f)));
			Point3D c = new Point3D(0,0,0);
			if (linker.mash != null)
				c = checkCollision(0.3f);
			if (c.sqrt() > 0) {
				p.pos = p.pos.add(c);
				
				s = s.add(c.mul(1f));
				//if (!input.isKeyPressed(KeyEvent.VK_SPACE)) {
					s.x *= 0.8;
					s.z *= 0.8;
				//}
				s.y *= 0.6;
				canjump = true;
				break;
			}
		}
		
		if (!Console.inchat && canjump && input.isKeyPressed(KeyEvent.VK_SPACE)) {
			s.y += 0.1f;
		}
		
		p.dir = p.dir.add(r); // .add(rm)
		if (p.dir.x < -90) p.dir.x = -90;
		if (p.dir.x > 90) p.dir.x = 90;
		
		
		s = s.add(g);
		s.x *= 0.85;
		s.z *= 0.85;
		if (s.y > 0)
		s.y *= 0.99;
		/*s.x = Math.min(Math.max(s.x, -0.19f), 0.19f);
		s.y = Math.min(Math.max(s.y, -0.19f), 0.19f);
		s.z = Math.min(Math.max(s.z, -0.19f), 0.19f);
		*/
		
		console.processInput("~tpp " + Console.name + " " + p.pos.x + " " + (p.pos.y-0.4f) + " " + p.pos.z + " " + 0 + " " + (p.dir.y+90), false);
		Point3D p = new Point3D(s.x, 0, s.z);
		console.processInput("~setanim " + Console.name + " " + ((p.sqrt() > 0.001) ? "run" : "stand"), false);
	}
	
	float height = 0.1f;
	
	private Point3D checkCollision(float r) {
		Point3D spos = new Point3D(p.pos);
		spos.y -= height;
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
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    