package org.tuvaya.test;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import org.tuvaya.connect.Client;
import org.tuvaya.engine.Gameloop;
import org.tuvaya.engine.window.Display;
import org.tuvaya.engine.window.inputs.*;
import org.tuvaya.engine.gameobjects.Element;
import org.tuvaya.engine.gameobjects.Scene;
import org.tuvaya.engine.gameobjects.Tag;
import org.tuvaya.engine.tags.Console;
import org.tuvaya.engine.tags.NoCollision;
import org.tuvaya.engine.tags.ObjectAnimationTag;
import org.tuvaya.engine.tags.PlayerMovementTag;
import org.tuvaya.engine.tags.Render3DTag;
import org.tuvaya.engine.types.Point3D;
import org.tuvaya.engine.types.elements.Camera;
import org.tuvaya.engine.types.elements.Linker;
import org.tuvaya.engine.types.elements.Object3D;
import org.tuvaya.engine.types.elements.Sprite3D;

public class Game {
	
	public static int width = 800, height = 600; 
	public static String title = "3DEngine";
	public static Display window;
	public static Gameloop game;
	public static Graphics2D g;
	public static Keyboard input;
	public static Mouse minput;
	public static boolean full = true;
	public static String name = "Player-" + Math.abs(new Random().nextInt(100));
	static int y = 20;
	
	public static String ip = "localhost";
	public static int port = 25565;
	
	public static void main(String[] args) {
		
		if (args.length > 0) {
			for (String a : args) {
				String[] p = a.split("=");
				String key = p[0];
				String value = p[1];
				switch (key) {
					case "name" -> name = value;
					case "rscale" -> Render3DTag.scale = Integer.valueOf(value);
					case "ip" -> ip = value;
					case "port" -> port = Integer.valueOf(value);
					case "full" -> full = Boolean.valueOf(value);
				}
			}
		}
		
		if (full) {
			width = Toolkit.getDefaultToolkit().getScreenSize().width;
			height = Toolkit.getDefaultToolkit().getScreenSize().height;
		}
		
		window = new Display(width, height, title, full);
		g = window.g;
		window.addInput(input = new Keyboard());
		window.addInput(minput = new Mouse());
		
		window.window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				game.stop();
			}
		});
		
		
		game = new Gameloop();
		
		game.addMainHook(() ->{
			window.clear();
			try {
				game.getCurrentScene().Render();
			} catch (Exception e) {
				
				game.stop(e);
			}
			if (input.isKeyPressed(KeyEvent.VK_ALT)
					&& input.isKeyPressed(KeyEvent.VK_F4)) game.stop();
			window.swapBuffers();
			window.setTitle("(" + game.FPS + ") " + title);
		});
		
		Scene scene = new Scene();
		game.addScene(scene);
		game.sceneNo = 0;
		
		game.addStopHook(()->{
			scene.get("camera").getTag(Console.class).processInput("~remove " + Console.name, false);
			scene.get("camera").getTag(Console.class).processInput("~say \"" + Console.name + " left\"", false);
			scene.get("connect", Client.class).close();
		});
		
		try {
			scene.add("connect", new Client(ip, port));
			//scene.add("connect", new Client("89.169.172.36", 26302));
		} catch (IOException e1) {
			game.stop();
		}
		/*scene.add("collisey", new Object3D("assets/models/colodec.obj", new Point3D(0,0,0)));
		scene.add("Tuvaya", new Object3D("assets/models/tuvaya2.obj", new Point3D(0,0,5)));
		scene.get("Tuvaya", Object3D.class).dir = new Point3D(0,-90,0);*/
		scene.add("map", new Object3D("assets/models/coolmap.obj", new Point3D(0,0,0)));
		
		for (int i = 0; i < 20; i++) {
			final int I = i;
			scene.add("cube" + I, new Object3D("assets/models/cube.obj", new Point3D((float)(Math.random()*-60)+20,(float)(Math.random()*10)+10,(float)(Math.random()*-60)+20)))
			.addTag(new Tag() {
				
				Object3D cube = scene.get("cube" + I, Object3D.class);
				Point3D d = new Point3D((float)Math.random(), (float)Math.random(), 0);
				
				
				@Override
				public void action() throws Exception {
					cube.dir = cube.dir.add(d);
				}
				
			});
		}
		
		for (int i = 0; i < 10; i++) {
			final int I = i;
			
			scene.add("cloud" + I, new Object3D("assets/models/cloud.obj", new Point3D((float)(Math.random()*-60)+20,(float)(Math.random()*20)+25,(float)(Math.random()*-60)+20)))
			.addTag(new Tag() {
				
				Object3D cube = scene.get("cloud" + I, Object3D.class);
				Point3D d = new Point3D((float)Math.random()*0.2f, 0, 0);
				
				
				@Override
				public void action() throws Exception {
					cube.pos = cube.pos.add(d);
					if (cube.pos.x > 20) cube.pos.x = -50;
				}
				
			})
			;
			scene.get("cloud" + I, Object3D.class).dir = new Point3D(0,(float)Math.random()*360,0);
		}
		
		scene.add("linker", new Linker(scene));
		scene.add("camera", new Camera(new Point3D(1,1,1), width, height, scene))
		.addTag(new PlayerTag())
		.addTag(new Console(input, name, game,scene.get("camera", Camera.class)))
		.addTag(new PlayerMovementTag(scene.get("camera", Object3D.class), input, (full) ? minput : null, window.window.getLocation(), scene.get("linker", Object3D.class)))
		.addRenderTag(()->{
			g.drawImage(scene.get("camera", Camera.class).render.getScaledInstance(width, height, 0), 0, 0, null);
			g.setColor(Color.gray);
			
		})
		.addTag(new WeaponUpdateTag(scene.get("camera", Camera.class), minput, input))
		;
		scene.get("camera").getTag(PlayerTag.class).name = Console.name;
		
		scene.add("weapon", new Object3D("", new Point3D(0,0,0))).addTag(new WeaponsTag(scene.get("weapon", Object3D.class), scene.get("camera", Object3D.class))).addTag(new NoCollision());
		scene.get("weapon", Object3D.class).scale = 0.03f;
		
		Console.addComand("setdm",new Console.Command() {

			@Override
			public boolean connect() {
				return true;
			}

			@Override
			public String exec(String[] args, Gameloop game) throws Exception {
				
				PlayerTag.time = 60*60;
				
				scene.add("pistol1", new Object3D("assets/models/weapon/pistol.obj", new Point3D(-6.550f,5.439f,3.488f))).addTag(new Getable(scene.get("pistol1", Object3D.class), scene.get("camera", Object3D.class), ()->{
					scene.get("camera", Camera.class).getTag(Console.class).processInput("~getw pistol", true);}));
				scene.add("pistol2", new Object3D("assets/models/weapon/pistol.obj", new Point3D(3.677f,4.844f,-6.424f))).addTag(new Getable(scene.get("pistol2", Object3D.class), scene.get("camera", Object3D.class), ()->{
					scene.get("camera", Camera.class).getTag(Console.class).processInput("~getw pistol", true);}));
				scene.add("revolver1", new Object3D("assets/models/weapon/revolver.obj", new Point3D(5.461f,5.189f,-28.668f))).addTag(new Getable(scene.get("revolver1", Object3D.class), scene.get("camera", Object3D.class), ()->{
					scene.get("camera", Camera.class).getTag(Console.class).processInput("~getw revolver", true);}));
				scene.add("revolver2", new Object3D("assets/models/weapon/revolver.obj", new Point3D(-29.155f,0.356f,7.115f))).addTag(new Getable(scene.get("revolver2", Object3D.class), scene.get("camera", Object3D.class), ()->{
					scene.get("camera", Camera.class).getTag(Console.class).processInput("~getw revolver", true);}));
				scene.add("ak1", new Object3D("assets/models/weapon/ak47.obj", new Point3D(-5.270f,5.193f,-30.389f))).addTag(new Getable(scene.get("ak1", Object3D.class), scene.get("camera", Object3D.class), ()->{
					scene.get("camera", Camera.class).getTag(Console.class).processInput("~getw ak47", true);}));
				scene.add("ak2", new Object3D("assets/models/weapon/ak47.obj", new Point3D(-25.831f,0.356f,-9.261f))).addTag(new Getable(scene.get("ak2", Object3D.class), scene.get("camera", Object3D.class), ()->{
					scene.get("camera", Camera.class).getTag(Console.class).processInput("~getw ak47", true);}));
				scene.add("rifle1", new Object3D("assets/models/weapon/rifle.obj", new Point3D(-22.744f,2.276f,-22.700f))).addTag(new Getable(scene.get("rifle1", Object3D.class), scene.get("camera", Object3D.class), ()->{
					scene.get("camera", Camera.class).getTag(Console.class).processInput("~getw rifle", true);}));
				scene.add("minigun1", new Object3D("assets/models/weapon/minigun.obj", new Point3D(-17.512f,13.973f,-30.252f))).addTag(new Getable(scene.get("minigun1", Object3D.class), scene.get("camera", Object3D.class), ()->{
					scene.get("camera", Camera.class).getTag(Console.class).processInput("~getw minigun", true);}));
				
				scene.add("dm", new Element()).addTag(()->{
					if (PlayerTag.time <= 0) {
						scene.get("camera").getTag(Console.class).processInput("~unsetdm", false);
					}
				});
				
				return "";
			}
		
		});
		
		Console.addComand("unsetdm",new Console.Command() {

			@Override
			public boolean connect() {
				return true;
			}

			@Override
			public String exec(String[] args, Gameloop game) throws Exception {
		
				scene.elements.remove("pistol1");
				scene.elements.remove("pistol2");
				scene.elements.remove("revolver1");
				scene.elements.remove("revolver2");
				scene.elements.remove("ak1");
				scene.elements.remove("ak2");
				scene.elements.remove("rifle1");
				scene.elements.remove("minigun1");
				scene.elements.remove("dm");
				
				return "";
			}
		
		});
		
		Console.addComand("setchase", new Console.Command() {

			@Override
			public boolean connect() {
				return true;
			}

			@Override
			public String exec(String[] args, Gameloop game) throws Exception {
				scene.add("chase", new Object3D("assets/models/ring.obj", new Point3D(0,0,0)))
				.addTag(new NoCollision())
				.addTag(new Tag() {
					
					Point3D[] chase = new Point3D[] {
							new Point3D(-1.2067717f, 7.539842f, -4.628546f),
							new Point3D(-2.3842084f, 7.176544f, -10.341797f),
							new Point3D(-10.489448f, 7.124145f, -12.557838f),
							new Point3D(-12.222193f, 7.647972f, -22.223795f),
							new Point3D(-24.049135f, 6.0339284f, -22.337097f),
							new Point3D(-25.918255f, 5.862104f, -17.96483f),
							new Point3D(-26.652504f, 7.128564f, -10.318129f),
							new Point3D(-10.270876f, 7.124145f, -10.65553f),
							new Point3D(-10.626971f, 7.124144f, -22.482788f),
							new Point3D(-17.820333f, 6.046734f, -22.903944f),
							new Point3D(-17.37791f, 5.884715f, -27.220991f),
							new Point3D(-22.489824f, 2.2851968f, -26.924255f),
							new Point3D(-24.990343f, 2.5140648f, -19.521147f),
							new Point3D(-27.289553f, 7.1763515f, -10.519905f),
							new Point3D(-29.96602f, 0.43045998f, -1.7697961f),
							new Point3D(-28.050282f, 0.4091699f, 8.066181f),
							new Point3D(-22.194633f, 0.35642436f, 1.5241202f),
							new Point3D(-14.23097f, 0.35642436f, 1.5863016f),
							new Point3D(-10.679442f, 1.079995f, 0.62956464f),
							new Point3D(-1.6490117f, 0.5741367f, -1.0348424f),
							new Point3D(3.2357535f, 0.5568864f, 4.4866147f),
							new Point3D(1.0080129f, 5.074489f, 1.432283f),
							new Point3D(-1.4251609f, 7.124073f, 0.52415055f)
					};
					
					int no = 0;
					Object3D s = scene.get("chase", Object3D.class);
					Object3D p = scene.get("camera", Object3D.class);
					
					float time = 0;
					
					@Override
					public void action() throws Exception {
						time += 1/60f;
						s.pos = chase[no];
						s.scale = 0.5f;
						if (s.pos.sub(p.pos).sqrt() <= 0.7f) {
							no++;
							if (no == chase.length) {
								p.getTag(Console.class).processInput("~add " + Console.name + " bt " + time, false);
								scene.elements.remove("chase");
							}
						}
						s.dir = s.dir.add(new Point3D(5,4,0));
					}
					
				});
				
				return "";
			}
		
		});
		
		Console.addComand("unsetchase", new Console.Command() {

			@Override
			public boolean connect() {
				return true;
			}

			@Override
			public String exec(String[] args, Gameloop game) throws Exception {
				scene.elements.remove("chase");
				return "";
			}
		
		});
		
		scene.get("camera", Camera.class).ondeath = ()->{
			scene.get("camera").getTag(Console.class).processInput("~add " + Console.name + " -flags", false);
		};
		
		scene.add("tablo", new Sprite3D("assets/sprite/dumbfox.png", new Point3D(-11,20,-11)))
		.addTag(()->{
			Sprite3D s = scene.get("tablo", Sprite3D.class);
			if (s.texture.getWidth() < 20)
				s.texture = new BufferedImage(250,150,2);
			Graphics2D g = s.texture.createGraphics();
			g.setColor(Color.black);
			g.fillRect(0, 0, 1000, 1000);
			g.setColor(Color.white);
			g.drawString("Timer: " + PlayerTag.time, 0, 10);
			g.drawString("Name : Deaths : Kills : Flags : BestTime",0,20);
			y = 30;
			scene.forEach(Object3D.class, (obj)->{
				if (obj.getTag(PlayerTag.class) == null) return;
				PlayerTag p = obj.getTag(PlayerTag.class);
				g.drawString(p.name + " " + p.deaths + " " + p.kills + " " + p.flags + " " + p.besttime, 0, y);
				y+=10;
			});
		})
		;
		scene.get("tablo", Sprite3D.class).scale = 0.05f;
		
		/*scene.add("player", new Object3D("assets/models/player/body.obj", new Point3D(1.3f,5.07f-0.4f,1.2f)))
		.addTag(new ObjectAnimationTag(scene.get("player", Object3D.class), "assets/models/tuvaya/"))
		;
		scene.get("player", Object3D.class).scale = 0.1f;*/
		
		//scene.add("fox", new Sprite3D("assets/sprites/dumbfox.png", new Point3D(0,8,0)));
		//scene.get("fox", Sprite3D.class).scale = 10f;
		Console.addComand("add",new Console.Command() {

			@Override
			public boolean connect() {
				return true;
			}

			@Override
			public String exec(String[] args, Gameloop game) throws Exception {
				Object3D obj = null;
				if (args[0].equals(Console.name)) {
					obj = scene.get("camera", Object3D.class);
				}else {
					obj = scene.get(args[0], Object3D.class);
				}
				PlayerTag p = obj.getTag(PlayerTag.class);
				switch (args[1]) {
					case "deaths" -> p.deaths++;
					case "kills" -> p.kills++;
					case "flags" -> p.flags++;
					case "bt" -> p.besttime = Float.valueOf(args[2]);
					case "-deaths" -> p.deaths = 0;
					case "-kills" -> p.kills = 0;
					case "-flags" -> p.flags = 0;
					case "-bt" -> p.besttime = 0;
					case "-" -> {p.deaths = 0; p.flags = 0; p.kills = 0; p.besttime = 0;}
				}
				return "";
			}
			
		});
		Console.addComand("shoot",new Console.Command() {

			@Override
			public boolean connect() {
				return true;
			}

			@Override
			public String exec(String[] args, Gameloop game) throws Exception {
				scene.add("bullet" + System.currentTimeMillis(), new Bullet("bullet" + System.currentTimeMillis(),scene, new Point3D(
						Float.parseFloat(args[0]),
						Float.parseFloat(args[1]),
						Float.parseFloat(args[2])),
						new Point3D(Float.parseFloat(args[3]),
									Float.parseFloat(args[4]),
									Float.parseFloat(args[5])),
						Float.parseFloat(args[6]),
						args[7]));
				return "";
			}
			
		});
		Console.addComand("getw",new Console.Command() {

			@Override
			public boolean connect() {
				return true;
			}

			@Override
			public String exec(String[] args, Gameloop game) throws Exception {
				switch (args[0]) {
					case "pistol" -> WeaponUpdateTag.weapons[0] = Weapon.pistol;
					case "revolver" -> WeaponUpdateTag.weapons[1] = Weapon.revolver;
					case "ak47" -> WeaponUpdateTag.weapons[2] = Weapon.ak47;
					case "rifle" -> WeaponUpdateTag.weapons[3] = Weapon.rifle;
					case "minigun" -> WeaponUpdateTag.weapons[4] = Weapon.minigun;
					case "-" -> WeaponUpdateTag.weapons = new Weapon[]{null,null,null,null,null,null};
				}
				return "";
			}
			
		});
		
		/*scene.add("test", new Object3D("", new Point3D(1.3f,5.07f-0.4f,1.2f)))
		.addTag(new ObjectAnimationTag(scene.get("test", Object3D.class), "assets/models/tuvaya/"))
		.addTag(new PlayerTag())
		;*/
		
		BufferedImage cursorImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

		// 2. Создаем из него невидимый курсор
		Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(
		    cursorImg, 
		    new Point(0, 0), 
		    "blank cursor"
		);	
		
		// 3. Применяем его к окну или холсту (canvas / frame)
		window.content.setCursor(blankCursor);
		
		game.start();
		
	}
	
}
