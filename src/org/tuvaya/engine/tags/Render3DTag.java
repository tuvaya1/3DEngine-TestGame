package org.tuvaya.engine.tags;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Arrays;

import org.tuvaya.engine.gameobjects.Scene;
import org.tuvaya.engine.gameobjects.Tag;
import org.tuvaya.engine.types.Point3D;
import org.tuvaya.engine.types.Triangle;
import org.tuvaya.engine.types.elements.Camera;
import org.tuvaya.engine.types.elements.Object3D;
import org.tuvaya.engine.types.elements.Sprite3D;

public class Render3DTag implements Tag {

	public static final int ware = 0;
	public static final int solid = 1;
	
	public static int mode = solid;
	
	int width, height;
	Graphics2D g;
	Camera p;
	int[] raster;
	float[] zbuffer;
	Object3D linked;
	
	Point3D half = new Point3D(0.5f,0.5f,0f);
	Point3D res;
	
	class Plane {
		Point3D normal;
		float D;
		public Plane(Point3D normal, float D) {this.normal = normal; this.D = D;}
	}
	
	static float fov = 120f;
	float aspect;
	float near = 0.1f;
	float far = 1000f;
	
	float halfW;
	float halfH;

	Plane[] planes;
	
	public static int scale = 2;
	
	Scene scene;
	
	public Render3DTag(int width, int height, Camera player, Scene scene) {
		this.width = width/scale;
		this.height = height/scale;
		this.p = player;
		p.render = new BufferedImage(this.width,this.height,2);
		this.g = (Graphics2D) p.render.getGraphics();
		raster = ((DataBufferInt)p.render.getRaster().getDataBuffer()).getData();
		zbuffer = new float[raster.length];
		this.scene = scene;
		this.linked = scene.get("linker", Object3D.class).getTag(LinkerTag.class).render;
		// 1. Стандартный аспект (ширина / высота)
		aspect = (float)this.width / this.height;
		
		// 2. Половина высоты и ширины экрана на расстоянии Z = 1.0
		halfH = (float)Math.tan(Math.toRadians(fov / 2));
		halfW = halfH * aspect;
		
		// 3. Разрешение экрана (теперь без деления на аспект!)
		res = new Point3D(this.width, this.height, 0f);
		
		// 4. Сдвиг строго по центру экрана
		half = new Point3D(0.5f, 0.5f, 0f);
		
		setPlanes();
	}
	
	private void setPlanes() {
		// БОКОВЫЕ ПЛОСКОСТИ ТЕПЕРЬ НАКЛОНЕНЫ (D = 0, проходят через камеру)
		planes = new Plane[] {
			    new Plane(new Point3D(1, 0, -halfW-0.0001f), 0),    // Левая плоскость (наклон влево)
			    new Plane(new Point3D(-1, 0, -halfW), 0),   // Правая плоскость (наклон вправо)
			    new Plane(new Point3D(0, 1, -halfH-0.0001f), 0),    // Нижня плоскость (наклон вниз)
			    new Plane(new Point3D(0, -1, -halfH), 0),   // Верхняя плоскость (наклон вверх)
			    new Plane(new Point3D(0, 0, -1), -near),    // Ближняя плоскость (Z <= -near)
			    new Plane(new Point3D(0, 0, 1), far)        // Дальняя плоскость (Z >= -far)
			};
	}

	float f = 1f / (float)Math.tan(Math.toRadians(fov / 2));
	private int backgroundcolor = 0xFF9fe4f4;
	
	float framerate = 60f;
	float frame = 1000/framerate;
	static float delta;
	
	@Override
	public void action() throws Exception {
		
		Arrays.fill(raster, backgroundcolor);
		Arrays.fill(zbuffer, Float.MAX_VALUE);
		
		//System.out.println(linked.mash.triangres.length);
		long time = System.currentTimeMillis();
		renderObject(linked);
		renderSprites();
		time = System.currentTimeMillis()-time;
		delta = frame/time;
		float fps = (time > 0) ? 1000/time : 0;
		
		if (Console.inchat) {
			g.setColor(new Color(0x8000FF00, true));
			g.fillRect(0, height-10, width, 10);
		}
		
		if (getb(p.render.getRGB(width/2, height/2)) > 0.5f) {
			g.setColor(Color.black);
		}else {
			g.setColor(Color.white);
		}
		g.drawLine(width/2-5, height/2, width/2+5, height/2);
		g.drawLine(width/2, height/2-5, width/2, height/2+5);
		
		
		g.setColor(Color.gray);
		//g.drawLine(0, height/2, width, height/2);
		g.drawString("fps : " + fps,0,10);
		g.drawString("delta : " + delta,0,20);
		g.drawString("render res : " + width + "x" + height,0,30);
		g.drawString("scale : " + 1f/scale,0,40);
		g.drawString("triangles : " + linked.mash.triangres.length,0,50);
		g.drawString("pos : " + p.pos.toString(),0,60);
		g.drawString("dir : " + p.dir.toString(),0,70);
		Point3D s = PlayerMovementTag.s;
		Point3D p = new Point3D(s.x, 0, s.z);
		g.drawString("speed : " + (int)(p.sqrt()*1000)/1000f,0,80);
		g.drawString("name : " + Console.name,0,90);
		for (int i = 0; i < 20; i++) {
			if (i >= Console.chat.size()) break;
			String l = Console.chat.get(Console.chat.size()-i-1);
			g.drawString(l,0,height-i*10-10);
		}
		g.setColor(Color.DARK_GRAY);
		g.drawString(Console.in, 0, height);
	}
	
	private void renderSprites() {
	    scene.forEach(Sprite3D.class, (s) -> {
	        Point3D pos = s.pos.sub(p.pos).rotateinv(p.dir).mul(new Point3D(-1,1,1));
	        
	        // RHS: перед камерой Z < 0
	        if (pos.z > -near) return;
	        
	        float invZ = 1f / (-pos.z);
	        float scaleX = invZ / (2f * halfW);
	        float scaleY = invZ / (2f * halfH);
	        
	        float p0x = (pos.x * scaleX + half.x) * res.x;
	        float p0y = (-pos.y * scaleY + half.y) * res.y; // -pos.y для RHS
	        
	        float tx = s.texture.getWidth()*s.scale;
	        float ty = s.texture.getHeight()*s.scale;
	        
	        float p1x = ((pos.x+tx) * scaleX + half.x) * res.x;
	        float p1y = ((-pos.y+ty) * scaleY + half.y) * res.y;
	        
	        int sw = (int)(p1x-p0x);//(int)(s.texture.getWidth() * scaleX * s.scale);
	        int sh = (int)(p1y-p0y);//(int)(s.texture.getHeight() * scaleX * s.scale);
	        
	        if (sw <= 0 || sh <= 0 || sw > width * 2 || sh > height * 2) return;
	        
	        // Кэшированная текстура (не пересоздаётся каждый кадр!)
	        BufferedImage texture = scaled(s.texture, sw, sh);
	        
	        drawtexture(p0x - (sw / 2f), p0y - (sh / 2f), -pos.z, texture);
	    });
	}

	private BufferedImage scaled(BufferedImage texture, int sw, int sh) {
	    int w = texture.getWidth();
	    int h = texture.getHeight();
	    float dw = (float) w / sw;
	    float dh = (float) h / sh;
	    BufferedImage ret = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
	    for (int i = 0; i < sw; i++) {
	        int sx = Math.min(w - 1, (int) (i * dw));
	        for (int j = 0; j < sh; j++) {
	            int sy = Math.min(h - 1, (int) (j * dh));
	            ret.setRGB(i, j, texture.getRGB(sx, sy));
	        }
	    }
	    return ret;
	}
	
	private void drawtexture(float x, float y, float z, BufferedImage texture) {
		for (int i = 0; i < texture.getWidth(); i++) {
			for (int j = 0; j < texture.getHeight(); j++) {
				int sx = (i+(int)x);
				int sy = (j+(int)y);
				if (sx < 0 || sx >= width || sy < 0 || sy >= height) continue;
				
				int idx = sx+sy*width;
				
				if (z < zbuffer[idx]) {
					int color = texture.getRGB(i, j);
			        //if (((color << 24) & 0xFF) > 0) {
			        	raster[idx] = color;
						zbuffer[idx] = z;
			        //}
				}
			}
		}
	}

	private float getb(int rgb) {
		float r = ((rgb >> 16) & 0xFF) / 255f;
		float g = ((rgb >> 8) & 0xFF) / 255f;
		float b = ((rgb) & 0xFF) / 255f;
		return 0.299f*r + 0.587f*g + 0.114f*b;
	}

	int vis ;
	int vtis;
	int vnis;

	

	private void renderObject(Object3D obj) {
		
		Point3D playerview = new Point3D(0,0,1).rotate(p.dir);
		
		Point3D[] v = new Point3D[obj.mash.vertices.length];
		for (int i = 0; i < v.length; i++) v[i] = obj.mash.vertices[i];
		
		for (int i = 0; i < v.length; i++) v[i] = v[i].sub(p.pos).rotateinv(p.dir);
		
		for (Triangle tr : obj.mash.triangres) {
			Point3D vc0 = v[(int)tr.v0].mul(new Point3D(-1,-1,1));
			Point3D vc1 = v[(int)tr.v1].mul(new Point3D(-1,-1,1));
			Point3D vc2 = v[(int)tr.v2].mul(new Point3D(-1,-1,1));
			
			Point3D center = obj.mash.vertices[(int)tr.v0]
					.add(obj.mash.vertices[(int)tr.v1])
					.add(obj.mash.vertices[(int)tr.v2]).mul(1/3f);
			
			if (center.sub(p.pos).rotateinv(p.dir).z > near) {
				//System.out.println("!");
				//continue;
			}
			
			Point3D n0 = obj.mash.normales[(int)tr.n0];
			Point3D n1 = obj.mash.normales[(int)tr.n1];
			Point3D n2 = obj.mash.normales[(int)tr.n2];
			
			Point3D norm = n0.add(n1).add(n2).mul(1/3f).norm();
			
			if (norm.dot(center.sub(p.pos)) >= 0f) continue;
				
			
			float l = norm.dot(new Point3D(0.8f,1f,0.5f).norm());
			//l -= norm.dot(playerview.mul(-1))*0.5f;
			
			l = Math.max(l+0.1f, 0.3f);
			
            // Вызываем быстрый клиппинг без выделения памяти
			clippedResult[0] = vc0;
			clippedResult[1] = vc1;
			clippedResult[2] = vc2;
            int vertexCount = clipTriangle(vc0, vc1, vc2, clippedResult);
            if (vertexCount == 0) continue; // Полностью отсечен
            
            // Проходим по результату шагом по 3 (рисуем готовые треугольники)
            for (int i = 0; i < vertexCount; i += 3) {
                
                Point3D v0 = clippedResult[i];
                Point3D v1 = clippedResult[i + 1];
                Point3D v2 = clippedResult[i + 2];
                
                // Сохраняем мировую Z ДО перспективного деления
                float z0 = v0.z;
                float z1 = v1.z;
                float z2 = v2.z;
    
                // Проецируем только X и Y
                float scaleX0 = 1f / (-z0 * 2f * halfW);
                float scaleY0 = 1f / (-z0 * 2f * halfH);
                
                float scaleX1 = 1f / (-z1 * 2f * halfW);
                float scaleY1 = 1f / (-z1 * 2f * halfH);
                
                float scaleX2 = 1f / (-z2 * 2f * halfW);
                float scaleY2 = 1f / (-z2 * 2f * halfH);
                
                // Центр экрана ТЕПЕРЬ СТРОГО half.x = 0.5f, half.y = 0.5f
                float p0x = (v0.x * scaleX0 + half.x) * res.x;
                float p0y = (v0.y * scaleY0 + half.y) * res.y;
    
                float p1x = (v1.x * scaleX1 + half.x) * res.x;
                float p1y = (v1.y * scaleY1 + half.y) * res.y;
    
                float p2x = (v2.x * scaleX2 + half.x) * res.x;
                float p2y = (v2.y * scaleY2 + half.y) * res.y;
                
                if (mode == ware) {
                    drawLine(p0x, p0y, p1x, p1y, brightness(tr.color, l));
                    drawLine(p1x, p1y, p2x, p2y, brightness(tr.color, l));
                    drawLine(p2x, p2y, p0x, p0y, brightness(tr.color, l));
                } else if (mode == solid) {
                    drawTriangle(p0x, p0y, z0, p1x, p1y, z1, p2x, p2y, z2, brightness(tr.color, l));
                }
            }
		}
		
	}
	
	// Буферы для промежуточных вычислений клиппинга (максимум 24 вершины на выходе)
	private final Point3D[] bufferA = new Point3D[24];
	private final Point3D[] bufferB = new Point3D[24];

	// Общий массив для записи финального результата триангуляции
	private final Point3D[] clippedResult = new Point3D[72]; 
	
	private int clipTriangle(Point3D v0, Point3D v1, Point3D v2, Point3D[] resultBuffer) {
	    // Копируем исходные 3 вершины в первый буфер
	    bufferA[0] = v0;
	    bufferA[1] = v1;
	    bufferA[2] = v2;
	    int inputCount = 3;

	    Point3D[] inputList = bufferA;
	    Point3D[] outputList = bufferB;

	    // Цикл по всем плоскостям отсечения
	    for (Plane p : planes) {
	        if (inputCount < 3) return 0; // Полигон выродился, рисовать нечего
	        
	        int outputCount = 0;

	        for (int i = 0; i < inputCount; i++) {
	            Point3D cur = inputList[i];
	            Point3D next = inputList[(i + 1) % inputCount];

	            // Расстояние до плоскости
	            float dCur = p.normal.x * cur.x + p.normal.y * cur.y + p.normal.z * cur.z + p.D;
	            float dNext = p.normal.x * next.x + p.normal.y * next.y + p.normal.z * next.z + p.D;

	            // Точка внутри плоскости видимости
	            if (dCur >= 0) {
	                outputList[outputCount++] = cur;
	            }
	            
	            // Линия пересекает плоскость -> вычисляем точку пересечения
	            if ((dCur >= 0) != (dNext >= 0)) {
	                float t = dCur / (dCur - dNext);
	                outputList[outputCount++] = new Point3D(
	                    cur.x + (next.x - cur.x) * t,
	                    cur.y + (next.y - cur.y) * t,
	                    cur.z + (next.z - cur.z) * t
	                );
	            }
	        }

	        // Меняем буферы местами для следующей плоскости
	        Point3D[] temp = inputList;
	        inputList = outputList;
	        outputList = temp;
	        inputCount = outputCount;
	    }

	    if (inputCount < 3) return 0;

	    // Триангуляция веером прямо в результирующий буфер
	    int resultIndex = 0;
	    for (int i = 1; i < inputCount - 1; i++) {
	        resultBuffer[resultIndex++] = inputList[0];
	        resultBuffer[resultIndex++] = inputList[i];
	        resultBuffer[resultIndex++] = inputList[i + 1];
	    }

	    return resultIndex; // Возвращаем количество записанных вершин (всегда кратно 3)
	}

	private boolean check(Point3D v, Plane plane) {
		return (v.z + near >= 0) &&           // ближняя
	              (-v.z - far >= 0) &&            // дальняя
	              (v.x + halfW >= 0) &&           // левая (x >= -halfW)
	              (-v.x + halfW >= 0) &&          // правая (x <= halfW)
	              (v.y + halfH >= 0) &&           // нижняя
	              (-v.y + halfH >= 0);            // верхняя
	}

	private int brightness(int color, float l) {
	    int a = (color >> 24) & 0xFF;
	    int r = (color >> 16) & 0xFF;
	    int g = (color >> 8) & 0xFF;
	    int b = color & 0xFF;
	    
	    r = Math.min(255, (int)(r * l));
	    g = Math.min(255, (int)(g * l));
	    b = Math.min(255, (int)(b * l));
	    
	    return (a << 24) | (r << 16) | (g << 8) | b;
	}

	private void drawTriangle(float p0x, float p0y, float p0z,
            float p1x, float p1y, float p1z,
            float p2x, float p2y, float p2z, int color) {

		// ========================================
		// ШАГ 1: Сортировка вершин по Y (сверху вниз)
		// ========================================
		// После сортировки: top.y <= mid.y <= bot.y
		// Используем простые обмены (bubble sort для 3 элементов),
		// чтобы не создавать массивы и объекты.
		
		float topX = p0x, topY = p0y, topZ = p0z;
		float midX = p1x, midY = p1y, midZ = p1z;
		float botX = p2x, botY = p2y, botZ = p2z;
		
		// Обмен top <-> mid
		if (topY > midY) {
		float tx = topX, ty = topY, tz = topZ;
		topX = midX; topY = midY; topZ = midZ;
		midX = tx;   midY = ty;   midZ = tz;
		}
		// Обмен top <-> bot
		if (topY > botY) {
			float tx = topX, ty = topY, tz = topZ;
			topX = botX; topY = botY; topZ = botZ;
			botX = tx;   botY = ty;   botZ = tz;
		}
		// Обмен mid <-> bot
		if (midY > botY) {
			float tx = midX, ty = midY, tz = midZ;
			midX = botX; midY = botY; midZ = botZ;
			botX = tx;   botY = ty;   botZ = tz;
		}
		
		// ========================================
		// ШАГ 2: Проверка вырожденных случаев
		// ========================================
		
		// Округляем Y до целых пикселей
		int yTop = Math.max(0, (int) Math.ceil(topY));
		int yBot = Math.min(height - 1, (int) Math.floor(botY));
		
		// Если треугольник полностью за экраном по вертикали — выходим
		if (yTop > yBot) return;
		
		// Если все три вершины на одной горизонтальной линии — это линия, не треугольник
		if (topY == botY) return;
		
		// ========================================
		// ШАГ 3: Подготовка инвертированных Z
		// ========================================
		// Для перспективно-корректной интерполяции работаем с 1/Z
		float invTopZ = 1.0f / topZ;
		float invMidZ = 1.0f / midZ;
		float invBotZ = 1.0f / botZ;
		
		// ========================================
		// ШАГ 4: Отрисовка верхней половины (top → mid)
		// ========================================
		// Вырожденный случай: если topY == midY, верхняя половина
		// имеет нулевую высоту — пропускаем её (деление на ноль!)
		if (midY > topY) {
			float dyUpper = midY - topY;       // Высота верхней половины
			float dyTotal = botY - topY;       // Полная высота треугольника
			
			// Шаг изменения X и 1/Z на каждом пикселе по Y
			// Левое ребро: top → mid
			float dxLeft  = (midX - topX) / dyUpper;
			float dzLeft  = (invMidZ - invTopZ) / dyUpper;
			
			// Правое ребро: top → bot (длинное ребро)
			float dxRight = (botX - topX) / dyTotal;
			float dzRight = (invBotZ - invTopZ) / dyTotal;
			
			// Начальные значения на верхней вершине
			float xLeft  = topX;
			float xRight = topX;
			float zLeft  = invTopZ;
			float zRight = invTopZ;
			
			// Сдвигаем начальные значения к первому целому пикселю
			float yStartOffset = yTop - topY;
			xLeft  += dxLeft  * yStartOffset;
			xRight += dxRight * yStartOffset;
			zLeft  += dzLeft  * yStartOffset;
			zRight += dzRight * yStartOffset;
			
			int yMid = Math.min(height - 1, (int) Math.floor(midY));
			
			for (int y = yTop; y <= yMid; y++) {
				// Определяем, какой край левый, какой правый
				float lx = xLeft, rx = xRight;
				float lz = zLeft, rz = zRight;
				if (lx > rx) {
					float tmp = lx; lx = rx; rx = tmp;
					tmp = lz; lz = rz; rz = tmp;
				}
				
				int xStart = Math.max(0, (int) Math.ceil(lx));
				int xEnd   = Math.min(width - 1, (int) Math.floor(rx));
				
				if (xStart <= xEnd) {
					float dxSpan = rx - lx;
					float dzSpan = rz - lz;
					float xOff = xStart - lx;
					
					for (int x = xStart; x <= xEnd; x++) {
					    float t = (dxSpan > 1e-6f) ? (xOff + (x - xStart)) / dxSpan : 0;
					    float invZ = lz + dzSpan * t;
					    float depth = -1.0f / invZ;
					
					    int idx = x + y * width;
					    if (depth < zbuffer[idx]) {
					        raster[idx] = color;
					        zbuffer[idx] = depth;
					    }
					}
				}
				
				xLeft  += dxLeft;
				xRight += dxRight;
				zLeft  += dzLeft;
				zRight += dzRight;
			}
		}
		
		// ========================================
		// ШАГ 5: Отрисовка нижней половины (mid → bot)
		// ========================================
		// Вырожденный случай: если midY == botY, нижняя половина
		// имеет нулевую высоту — пропускаем её (деление на ноль!)
		if (botY > midY) {
			float dyLower = botY - midY;       // Высота нижней половины
			float dyTotal = botY - topY;       // Полная высота треугольника
			
			// Левое ребро: mid → bot
			float dxLeft  = (botX - midX) / dyLower;
			float dzLeft  = (invBotZ - invMidZ) / dyLower;
			
			// Правое ребро: top → bot (то же длинное ребро)
			float dxRight = (botX - topX) / dyTotal;
			float dzRight = (invBotZ - invTopZ) / dyTotal;
			
			// Начальные значения на средней вершине
			float xLeft  = midX;
			float xRight = topX + dxRight * (midY - topY);
			float zLeft  = invMidZ;
			float zRight = invTopZ + dzRight * (midY - topY);
			
			// Сдвигаем к первому целому пикселю
			int yStart = Math.max((int) Math.ceil(midY), yTop);
			float yStartOffset = yStart - midY;
			xLeft  += dxLeft  * yStartOffset;
			xRight += dxRight * yStartOffset;
			zLeft  += dzLeft  * yStartOffset;
			zRight += dzRight * yStartOffset;
			
			for (int y = yStart; y <= yBot; y++) {
				float lx = xLeft, rx = xRight;
				float lz = zLeft, rz = zRight;
				if (lx > rx) {
					float tmp = lx; lx = rx; rx = tmp;
					tmp = lz; lz = rz; rz = tmp;
				}
				
				int xStart = Math.max(0, (int) Math.ceil(lx));
				int xEnd   = Math.min(width - 1, (int) Math.floor(rx));
				
				if (xStart <= xEnd) {
					float dxSpan = rx - lx;
					float dzSpan = rz - lz;
					float xOff = xStart - lx;
					
					for (int x = xStart; x <= xEnd; x++) {
					    float t = (dxSpan > 1e-6f) ? (xOff + (x - xStart)) / dxSpan : 0;
					    float invZ = lz + dzSpan * t;
					    float depth = -1.0f / invZ;
					
					    int idx = x + y * width;
					    if (depth < zbuffer[idx]) {
					        raster[idx] = color;
					        zbuffer[idx] = depth;
					    }
					}
				}
				
				xLeft  += dxLeft;
				xRight += dxRight;
				zLeft  += dzLeft;
				zRight += dzRight;
			}
		}
	}

	private void drawLine(float p0x, float p0y, float p1x, float p1y, int color) {
	    float x0 = p0x;
	    float y0 = p0y;
	    float x1 = p1x;
	    float y1 = p1y;
	    
	    float dx = x1 - x0;
	    float dy = y1 - y0;
	    
	    // Количество шагов = максимальное расстояние по оси
	    int steps = (int) Math.max(Math.abs(dx), Math.abs(dy));
	    if (steps == 0) {
	        // Точка
	        int x = Math.round(x0);
	        int y = Math.round(y0);
	        if (x >= 0 && x < width && y >= 0 && y < height) {
	            raster[x + y * width] = color;
	        }
	        return;
	    }
	    
	    float stepX = dx / steps;
	    float stepY = dy / steps;
	    
	    float x = x0;
	    float y = y0;
	    
	    for (int i = 0; i <= steps; i++) {
	        int px = Math.round(x);
	        int py = Math.round(y);
	        
	        if (px >= 0 && px < width && py >= 0 && py < height) {
	            raster[px + py * width] = color;
	        }
	        
	        x += stepX;
	        y += stepY;
	    }
	}

}
