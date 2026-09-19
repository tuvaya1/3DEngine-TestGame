package org.tuvaya.engine.tags;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.tuvaya.engine.gameobjects.Tag;
import org.tuvaya.engine.tags.Animation.Pose;
import org.tuvaya.engine.types.Mash;
import org.tuvaya.engine.types.Point3D;
import org.tuvaya.engine.types.Triangle;
import org.tuvaya.engine.types.elements.Object3D;

public class ObjectAnimationTag implements Tag {
	
	static HashMap<String, Animation> animations;
	
	HashMap<String, Mash> models;
	HashMap<String, Point3D> pos;
	ArrayList<String> names;
	
	String animation = "stand";
	int frame = 0;
	
	Object3D s;
	
	float scale = 1;
	
	public ObjectAnimationTag(Object3D s, String modelpath) {
		this.s = s;
		if (animations == null)
			loadAnimations();
		try {
			String model = Files.readString(Paths.get(modelpath + "skin.txt"));
			System.out.println(model);
			models = new HashMap<>();
			pos = new HashMap<>();
			names = new ArrayList<>();
			model = model.replaceAll("\r", "");
			String[] lines = model.split("\n");
			for (String l : lines) {
				//System.out.println();
				String[] parts = l.split(" ");
				if (parts[0].equals("scale")) {
					scale = Float.valueOf(parts[1]);
					continue;
				}
				models.put(parts[0], new Object3D(modelpath+parts[1], new Point3D(0,0,0)).mash);
				pos.put(parts[0], new Point3D(
						Float.parseFloat(parts[4]),
						Float.parseFloat(parts[3]),
						Float.parseFloat(parts[2])
						));
				names.add(parts[0]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean setAnimation(String name) {
		//System.out.println("Set animation" + name);
		if (animations.containsKey(name)) {
			if (!name.equals(animation)) {
				animation = name;
				frame = 0;
			}
			return true;
		}
		return false;
	}

	private void loadAnimations() {
		File dir = new File("assets/animations/");
		File[] files = dir.listFiles();
		animations = new HashMap<>();
		for (File f : files) {
			try {
				String anim = Files.readString(f.toPath());
				animations.put(f.getName().replace(".ani", ""), new Animation(anim));
				System.out.println("Add Animation : " + f.getName().replace(".ani", ""));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	float time = 0;
	
	@Override
	public void action() throws Exception {
		Animation.Pose pose = animations.get(animation).
				frames.get(frame);
		s.mash = link(pose);
		if ((int)time % (animations.get(animation).time/animations.get(animation).frames.size()) == 0) {
			if (frame < animations.get(animation).frames.size()-1)
			frame++;
			else {
				if (animations.get(animation).repeat) frame = 0;
			}
		}
		//System.out.println(time + " " + (animations.get(animation).time/animations.get(animation).frames.size()));
		time += 1;
	}

	private Mash link(Pose pose) {
		ArrayList<Point3D> v = new ArrayList<>();
	    ArrayList<Point3D> vt = new ArrayList<>();
	    ArrayList<Point3D> vn = new ArrayList<>();
	    ArrayList<Triangle> tr = new ArrayList<>();
	    
	    // Используем массивы для обхода ограничения "effectively final"
	    int[] vis = {0};
	    int[] vtis = {0};
	    int[] vnis = {0};
	    
	    for (String part : names) {
	    	Mash mash = models.get(part);
	        // Добавляем все вершины
	        for (Point3D p : mash.vertices) {
	            v.add(p.rotate(pose.pose.get(part)).add(pos.get(part)).mul(scale));
	        }
	        // Добавляем все текстурные координаты
	        for (Point3D p : mash.textures) {
	            vt.add(p);
	        }
	        // Добавляем все нормали
	        for (Point3D p : mash.normales) {
	            vn.add(p.rotate(pose.pose.get(part)));
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
	    }
	    
	    Mash mash = new Mash(
	        v.toArray(new Point3D[0]),
	        vt.toArray(new Point3D[0]),
	        vn.toArray(new Point3D[0]),
	        tr.toArray(new Triangle[0])
	    );
	    return mash;
	}
	
	
	
}

class Animation {
	
	ArrayList<Pose> frames;
	boolean repeat;
	int time = 1000;
	
	public Animation(String file) {
		frames = new ArrayList<>();
		file = file.replaceAll("\r", "");
		file = file.replaceAll("\n", "");
		String[] lines = file.split(";");
		for (String l : lines) {
			String[] parts = l.split("\\s+(?![^{]*})");
			if (parts[0].equals("repeat")) {
				repeat = Boolean.valueOf(parts[1]);
			}
			if (parts[0].equals("time")) {
				time = Integer.valueOf(parts[1]);
			}
			if (parts[0].equals("frame")) {
				frames.add(new Pose(parts[1].replace("{", "").replace("}", "")));
			}
		}
	}
	
	static class Pose {
		
		HashMap<String, Point3D> pose;
		
		public Pose(String file) {
			pose = new HashMap<>();
			String[] lines = file.split(",");
			for (String l : lines) {
				String[] parts = l.split(" ");
				pose.put(parts[0], new Point3D(
						Float.parseFloat(parts[1]),
						Float.parseFloat(parts[2]),
						Float.parseFloat(parts[3])
						));
			}
		}
		
	}
	
}