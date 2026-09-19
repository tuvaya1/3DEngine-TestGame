package org.tuvaya.engine.types.elements;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import org.tuvaya.engine.gameobjects.Element;
import org.tuvaya.engine.types.Mash;
import org.tuvaya.engine.types.Point3D;
import org.tuvaya.engine.types.Triangle;

public class Object3D extends Element {
	
	public Point3D pos;
	public Point3D dir = new Point3D(0,0,0); // градусы!
	public float scale = 1;
	
	public Mash mash;
	public int color = 0xFF00FF00;
	public BufferedImage image;
	
	public Mash getMash() {
		return mash;
	}
	
	public Object3D(String path, Point3D pos) {
		this.pos = pos;
		if (path.equals("")) {
			mash = null;
			return;
		}
		try {
			System.out.println("parse obj:" + path);
			String object = Files.readString(Paths.get(path));
			parseOBJFile(object, Paths.get(path).getParent().toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(color);
	}

	private void parseOBJFile(String object, String root) {
		object = object.replaceAll("\r", "");
		String[] lines = object.split("\n");
		
		ArrayList<Point3D> v = new ArrayList<>();
		ArrayList<Point3D> vt = new ArrayList<>();
		ArrayList<Point3D> vn = new ArrayList<>();
		ArrayList<Triangle> f = new ArrayList<>();
		
		HashMap<String, Integer> colors = new HashMap<>();
		int color = 0xFF000000;
		
		for (String l : lines) {
			if (l.isBlank() || l.startsWith("#")) continue;
			String[] parts = l.split(" ");
			if (parts[0].equals("mtllib")) {
				String path = root + "/" + parts[1];
				System.out.println("parse mat:" + path);
				try {
					String material = Files.readString(Paths.get(path));
					parseMTLFile(material, colors);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			if (parts[0].equals("usemtl")) {
				System.out.println(l);
				color = colors.get(parts[1]); 
				System.out.println(color);
			}
			if (parts[0].equals("v")) {
				v.add(new Point3D(
						Float.valueOf(parts[1]),
						Float.valueOf(parts[2]),
						Float.valueOf(parts[3])
						));
			}
			if (parts[0].equals("vt")) {
				vt.add(new Point3D(
						Float.valueOf(parts[1]),
						Float.valueOf(parts[2]),
						0
						));
			}
			if (parts[0].equals("vn")) {
				vn.add(new Point3D(
						Float.valueOf(parts[1]),
						Float.valueOf(parts[2]),
						Float.valueOf(parts[3])
						));
			}
			if (parts[0].equals("f")) {
				ArrayList<Integer> vi = new ArrayList<>();
				ArrayList<Integer> vni = new ArrayList<>();
				ArrayList<Integer> vti = new ArrayList<>();
				for (int i = 1; i < parts.length; i++) {
					String[] indexes = parts[i].split("/");
					vi.add(Integer.valueOf(indexes[0])-1);
					vni.add(Integer.valueOf(indexes[2])-1);
					if (!indexes[1].isBlank())
						vti.add(Integer.valueOf(indexes[1])-1);
					else
						vti.add(0);
				}
				for (int i = 0; i < vi.size() - 2; i++) {
					Triangle t = new Triangle(
				            vi.get(0), vi.get(i+1), vi.get(i+2),     // Вершины (было верно)
				            vni.get(0), vni.get(i+1), vni.get(i+2),  // Нормали (было неверно, чинил)
				            vti.get(0), vti.get(i+1), vti.get(i+2)   // UV (было неверно, чинил)
				        );
					t.color = color;
			        f.add(t);
			        
			    }
			}
			
		}
		
		Mash m = new Mash(v.toArray(new Point3D[0]), vt.toArray(new Point3D[0]), vn.toArray(new Point3D[0]), f.toArray(new Triangle[0]));
		this.mash = m;
	}

	private void parseMTLFile(String material, HashMap<String, Integer> colors) {
		material = material.replaceAll("\r", "");
		String[] lines = material.split("\n");
		String name = "";
		for (String l : lines) {
			if (l.isBlank() || l.startsWith("#")) continue;
			String[] parts = l.split(" ");
			if (parts[0].equals("newmtl")) {
				name = parts[1];
			}
			if (parts[0].equals("Kd")) {
				float r = Float.valueOf(parts[1]);
				float g = Float.valueOf(parts[2]);
				float b = Float.valueOf(parts[3]);
				System.out.println(r + " " + g + " " + b);
				int color = 0xFF000000 | ((int)(r*255) << 16) | ((int)(g*255) << 8) | ((int)(b*255));
				System.out.println(Integer.toHexString(color));
				colors.put(name, color);
			}
		}
	}
	
}
