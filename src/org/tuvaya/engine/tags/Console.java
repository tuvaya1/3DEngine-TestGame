package org.tuvaya.engine.tags;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

import org.tuvaya.connect.Client;
import org.tuvaya.engine.Gameloop;
import org.tuvaya.engine.gameobjects.Tag;
import org.tuvaya.engine.types.Point3D;
import org.tuvaya.engine.types.elements.Camera;
import org.tuvaya.engine.types.elements.Object3D;
import org.tuvaya.engine.window.inputs.Keyboard;
import org.tuvaya.test.Game;
import org.tuvaya.test.PlayerTag;

public class Console implements Tag {
	
	public static ArrayList<String> chat = new ArrayList<>();
	public static String in;
	
	public static boolean inchat = false;
	
	static HashMap<String, Command> comands;
	
	Keyboard input;
	
	public static String name;
	Gameloop game;
	
	Camera c;
	Client connect;
	
	public Console(Keyboard input, String name, Gameloop game, Camera camera) {
		this.c = camera;
		this.input = input;
		this.name = name;
		this.game = game;
		in = "";
		
		connect = game.getCurrentScene().get("connect", Client.class);
		comands = new HashMap<>();
		
		addComand("say", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
				return args[0];
			}

			@Override
			public boolean connect() {
				return true;
			}
		});
		addComand("tpp", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
				if (args.length < 4) return "[?] ~tp [name] [x] [y] [z]";
				String n = args[0];
				
				if (!g.getCurrentScene().elements.containsKey(n) && !n.equals(name)) {
					processInput("~setplayer " + n,true);
				}
				
				Float x = Float.valueOf(args[1]);
				Float y = Float.valueOf(args[2]);
				Float z = Float.valueOf(args[3]);
				Float rx = null;
				Float ry = null;
				if (args.length > 4) {
					if (args.length < 6) return "[?] ~tp [name] [x] [y] [z] <x> <y>";
					rx = Float.valueOf(args[4]);
					ry = Float.valueOf(args[5]);
				}
				try {
					g.getCurrentScene().get(n, Object3D.class).pos = new Point3D(x,y,z);
					if (rx != null)
					g.getCurrentScene().get(n, Object3D.class).dir = new Point3D(rx,ry,0);
				} catch (NullPointerException e) {
					return "";//"[!] cant find object :" + n; 
				}
				return "";
			}

			@Override
			public boolean connect() {
				// TODO Auto-generated method stub
				return true;
			}
		});
		addComand("tp", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
			if (args.length < 4) return "[?] ~tp [name] [x] [y] [z]";
			String n = args[0];
			Float x = Float.valueOf(args[1]);
			Float y = Float.valueOf(args[2]);
			Float z = Float.valueOf(args[3]);
			Float rx = null;
			Float ry = null;
			if (args.length > 4) {
				if (args.length < 6) return "[?] ~tp [name] [x] [y] [z] <x> <y>";
				rx = Float.valueOf(args[4]);
				ry = Float.valueOf(args[5]);
			}
			try {
				g.getCurrentScene().get(n, Object3D.class).pos = new Point3D(x,y,z);
				if (rx != null)
				g.getCurrentScene().get(n, Object3D.class).dir = new Point3D(rx,ry,0);
			} catch (NullPointerException e) {
				return "[!] cant find object :" + n; 
			}
			return "[.] teleport " + n + " to " + x + " " + y + " " + z;
			}

			@Override
			public boolean connect() {
				// TODO Auto-generated method stub
				return true;
			}
		});
		/*addComand("setname", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
			if (args.length < 1) return "[?] ~setname [name]";
			Console.name = args[0];
			return "[.] new name : " + name;
			}

			@Override
			public boolean connect() {
				// TODO Auto-generated method stub
				return false;
			}
		})*/;
		addComand("setrscale", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
			if (args.length < 1) return "[?] ~setrscale [scale]";
			try {
				Render3DTag.scale = Integer.valueOf(args[0]);
				camera.setRenderTag();
			} catch (NumberFormatException e) {
				return "[!] scale must be integer";
			}
			return "[.] Render scale : " + args[0];
			}

			@Override
			public boolean connect() {
				// TODO Auto-generated method stub
				return false;
			}
		});
		addComand("setrmode", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
			if (args.length < 1) return "[?] ~setrscale [solid/ware]";
			if (args[0].equals("solid")) {
				Render3DTag.mode = Render3DTag.solid;
			}else if (args[0].equals("ware")) {
				Render3DTag.mode = Render3DTag.ware;
			}else {
				return "[!] mode must be \"solid\" or \"ware\"";
			}
			return "[.] Render scale : " + args[0];
			}

			@Override
			public boolean connect() {
				// TODO Auto-generated method stub
				return false;
			}
		});
		addComand("setrfov", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
			if (args.length < 1) return "[?] ~setrfov [fov]";
			try {
				Render3DTag.fov = Integer.valueOf(args[0]);
				camera.setRenderTag();
			} catch (NumberFormatException e) {
				return "[!] fov must be integer";
			}
			return "[.] Render fov : " + args[0];
			}

			@Override
			public boolean connect() {
				return false;
			}
		});
		addComand("setrender", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
			if (args.length < 1) return "[?] ~setrender [camera name]";
			camera.setRenderTag();
			return "[.] Set Render";
			}

			@Override
			public boolean connect() {
				// TODO Auto-generated method stub
				return false;
			}
		});
		addComand("kill", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
			if (args.length < 1) return "[?] ~kill [name]";
			if (args[0].equals(name) || args[0].equals("@a")) {
				if (c.ondeath != null)
					c.ondeath.run();
				new Thread(()->{
						c.pos = new Point3D(-30,2.4f,-30f);
						try {
							Thread.sleep(1000*30);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						c.pos = new Point3D(1,10,1);
				}).start();
			}
			processInput("~say \"" + name + " was killed\"", false);
			return "you was killed";
			}

			@Override
			public boolean connect() {
				return true;
			}
		});
		addComand("setanim", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
			if (args.length < 1) return "[?] ~kill [name] [animation]";
			if (g.getCurrentScene().elements.containsKey(args[0])) {
				boolean set = g.getCurrentScene().get(args[0]).getTag(ObjectAnimationTag.class).setAnimation(args[1]);
				if (set) {
					return ""; //"[.] set animation : " + args[1] + " to : " + args[0];
				} else
					return "[!] undefined animation : " + args[1];
				}
			return "";
			}

			@Override
			public boolean connect() {
				// TODO Auto-generated method stub
				return true;
			}
		});
		addComand("setplayer", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
				if (args.length < 1) return "[?] ~setplayer [name]";
				if (!args[0].equals(name)) {
					g.getCurrentScene().add(args[0], new Object3D("", new Point3D(1.3f,5.07f-0.4f,1.2f)))
						.addTag(new ObjectAnimationTag(g.getCurrentScene().get(args[0], Object3D.class), "assets/models/player/"))
						.addTag(new PlayerTag())
					;
					g.getCurrentScene().get(args[0], Object3D.class).getTag(PlayerTag.class).name = args[0];
					g.getCurrentScene().get(args[0], Object3D.class).scale = 0.1f;
				}
				return args[0] + " joined";
			}

			@Override
			public boolean connect() {
				// TODO Auto-generated method stub
				return true;
			}
		});
		addComand("setskin", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
				if (args.length < 1) return "[?] ~setылskin [name] [skin]";
				if (!args[0].equals(name)) {
					g.getCurrentScene().get(args[0])
					.updateTags.removeIf(e -> e instanceof ObjectAnimationTag);
					g.getCurrentScene().get(args[0], Object3D.class)
					.addTag(new ObjectAnimationTag(g.getCurrentScene().get(args[0], Object3D.class), "assets/models/" + args[1] + "/"));
				}
				return "";
			}

			@Override
			public boolean connect() {
				// TODO Auto-generated method stub
				return true;
			}
		});
		addComand("remove", new Command() {
			public String exec(String[] args, Gameloop g) throws Exception {
				if (args.length < 1) return "[?] ~remove [name]";
				g.getCurrentScene().elements.remove(args[0]);
				return args[0] + " joined";
			}

			@Override
			public boolean connect() {
				// TODO Auto-generated method stub
				return true;
			}
		});
		
		
		//processInput("~setplayer " + name, false);
	}

	public static void addComand(String string, Command c) {
		comands.put(string, c);
	}

	int m;
	int u;
	int d;
	
	ArrayList<String> history = new ArrayList<>();
	
	int historyno = 0;
	
	String hin;
	
	@Override
	public void action() throws Exception {
		if (input.isKeyPressed(KeyEvent.VK_T) && !inchat) {
			inchat = true;
			input.clearBuffer();
			historyno = history.size();
			return;
		}
		if (input.isKeyPressed(KeyEvent.VK_ESCAPE)) inchat = false;
		
		if (inchat) {
			if (input.isKeyPressed(KeyEvent.VK_ENTER)) {
				processInput(in, false);
				
				history.add(in);
				in = "";
				inchat = false;
				
				return;
			}
			if (input.isKeyPressed(KeyEvent.VK_BACK_SPACE)){
				if (m < 0 && in.length() > 0) {
					in = in.substring(0,in.length()-1);
					m = 100;
					return;
				}
			}else {
				m = 0;
			}
			if (input.isKeyPressed(KeyEvent.VK_UP)) {
				if (u < 0 && in.length() > 0) {
					if (history.size() == 0) return;
					historyno --;
					if (historyno < 0) historyno = 0;
					if (historyno > history.size()) historyno = history.size();
					if (historyno == history.size()) in = hin;
					else in = history.get(historyno);
					u = 100;
					return;
				}
			}else {
				u = 0;
			}
			if (input.isKeyPressed(KeyEvent.VK_DOWN)) {
				if (d < 0 && in.length() > 0) {
					if (history.size() == 0) return;
					historyno ++;
					if (historyno < 0) historyno = 0;
					if (historyno > history.size()) historyno = history.size();
					if (historyno == history.size()) in = hin;
					else in = history.get(historyno);
					d = 100;
					return;
				}
			}else {
				d = 0;
			}
			if (input.getBuffer().length() > 0) {
				in += input.takeBuffer();
				hin = in;
			}
		}
		m--;
		u--;
		d--;
		
		if (connect != null) {
			if (connect.msgs.size() > 0) {
				connect.msgs.forEach((msg)->{
					if (msg.startsWith("~"))
						processInput(msg, true);
				});
				connect.msgs.clear();
			}
		}
		
	}

	public void processInput(String s, boolean fromServer) {
		if (fromServer)
			System.out.println(s);
		if (!s.startsWith("~")) {
			s = "~say \"<" + name + "> " + s + "\"";
		}
		ArrayList<String> trace = trace(s.replace("~", ""));
		if (comands.containsKey(trace.get(0))){
			Command c = comands.get(trace.get(0));
			try {
				String ret = c.exec(trace.subList(1, trace.size()).toArray(new String[0]), game);
				if (!ret.isBlank()) {
					chat.add(ret);
				}
				if (!ret.startsWith("[!]") && !fromServer && c.connect()) {
					
					connect.println(s);
				}
			} catch (Exception e) {
				chat.add("!ERROR! LOOK CONSOLE!");
				e.printStackTrace();
			}
		}else{
			chat.add("Undefined : " + s);
		}
	}
	
	private ArrayList<String> trace(String s) {
		ArrayList<String> ret = new ArrayList<>();
		String tmp = "";
		char c;
		int o = 0;
		boolean so = false;;
		for (int i = 0; i < s.length(); i++) {
			c = s.charAt(i);
			if (c == ' ' && !so && o == 0) {
				ret.add(tmp);
				tmp = "";
				continue;
			}
			if (c == '"') {so = !so; continue;}
			tmp += c;
		}
		ret.add(tmp);
		return ret;
	}

	public static interface Command {
		public boolean connect();
		public String exec(String[] args, Gameloop game) throws Exception;
	}
	
}
