package org.tuvaya.engine.types;

public class Point3D {
	
	public float x,y,z;
	
	public Point3D(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point3D(Point3D p) {
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
	}
	
	public Point3D add(Point3D p) {
		return new Point3D (x + p.x,
							y + p.y,
							z + p.z);
	}
	
	public Point3D sub(Point3D p) {
		return new Point3D (x - p.x,
							y - p.y,
							z - p.z);
	}
	
	public Point3D mul(float f) {
		return new Point3D (x * f,
							y * f,
							z * f);
	}
	
	public Point3D mul(Point3D p) {
		return new Point3D (x * p.x,
							y * p.y,
							z * p.z);
	}
	
	public float sqrt() {
		return (float) Math.sqrt(x*x+y*y+z*z);
	}
	
	public Point3D norm() {
		float s = sqrt();
		if (s == 0) return new Point3D(0,0,0);
		return new Point3D(x/s,y/s,z/s);
	}
	
	public float dot(Point3D p) {
		return x*p.x+y*p.y+z*p.z;
	}
	
	public Point3D cross(Point3D p) {
		return new Point3D(y*p.z-z*p.y, z*p.x-x*p.z, x*p.y-y*p.x);
	}
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (!(obj instanceof Point3D)) return false;
	    Point3D p = (Point3D) obj;
	    return Float.compare(x, p.x) == 0 &&
	           Float.compare(y, p.y) == 0 &&
	           Float.compare(z, p.z) == 0;
	}

	@Override
	public int hashCode() {
	    return Float.hashCode(x) ^ Float.hashCode(y) ^ Float.hashCode(z);
	}
	
	@Override
	public String toString() {
	    return String.format("(%.3f, %.3f, %.3f)", x, y, z);
	}

	public int getColor() {
		int x = (int)(this.x*255);
		if (x < 0) x = Math.abs(x)-128;
		int y = (int)(this.y*255);
		if (y < 0) y = Math.abs(y)-128;
		int z = (int)(this.z*255);
		if (z < 0) z = Math.abs(z)-128;
		
		
		
		return 0xFF000000 | (x << 16) | (y << 8) | (z);
	}
	
	public Point3D rotate(Point3D dir) {
	    // Преобразуем градусы в радианы
	    float angleX = (float) Math.toRadians(dir.x);
	    float angleY = (float) Math.toRadians(dir.y);
	    float angleZ = (float) Math.toRadians(dir.z);

	    float rx = x;
	    float ry = y;
	    float rz = z;

	    // 1. Поворот вокруг оси X (Pitch: положительный угол — смотрим ВВЕРХ)
	    float cosX = (float) Math.cos(angleX);
	    float sinX = (float) Math.sin(angleX);
	    float y1 = ry * cosX + rz * sinX;     // Изменен знак на ПЛЮС
	    float z1 = -ry * sinX + rz * cosX;    // Изменен знак на МИНУС
	    ry = y1;
	    rz = z1;        
	    
	    // 2. Поворот вокруг оси Y (Yaw: положительный угол — поворот ВПРАВО)
	    float cosY = (float) Math.cos(angleY);
	    float sinY = (float) Math.sin(angleY);
	    float x1 = rx * cosY + rz * sinY;
	    float z2 = -rx * sinY + rz * cosY;
	    rx = x1;
	    rz = z2;

	    // 3. Поворот вокруг оси Z (Roll)
	    float cosZ = (float) Math.cos(angleZ);
	    float sinZ = (float) Math.sin(angleZ);
	    float x2 = rx * cosZ - ry * sinZ;
	    float y2 = rx * sinZ + ry * cosZ;
	    rx = x2;
	    ry = y2;
	    
	    return new Point3D(rx, ry, rz);
	}

	public Point3D rotateinv(Point3D dir) {
	    // Преобразуем градусы в радианы
	    float angleX = (float) Math.toRadians(dir.x);
	    float angleY = (float) Math.toRadians(dir.y);
	    float angleZ = (float) Math.toRadians(dir.z);

	    float rx = x;
	    float ry = y;
	    float rz = z;

	    // 1. ОБРАТНЫЙ поворот вокруг оси Z (обратные знаки)
	    float cosZ = (float) Math.cos(angleZ);
	    float sinZ = (float) Math.sin(angleZ);
	    float x2 = rx * cosZ + ry * sinZ;     // Изменен знак на ПЛЮС
	    float y2 = -rx * sinZ + ry * cosZ;    // Изменен знак на МИНУС
	    rx = x2;
	    ry = y2;
	    
	    // 2. ОБРАТНЫЙ поворот вокруг оси Y (обратные знаки)
	    float cosY = (float) Math.cos(angleY);
	    float sinY = (float) Math.sin(angleY);
	    float x1 = rx * cosY - rz * sinY;     // Изменен знак на МИНУС
	    float z2 = rx * sinY + rz * cosY;     // Изменен знак на ПЛЮС
	    rx = x1;
	    rz = z2;

	    // 3. ОБРАТНЫЙ поворот вокруг оси X (обратные знаки)
	    float cosX = (float) Math.cos(angleX);
	    float sinX = (float) Math.sin(angleX);
	    float y1 = ry * cosX - rz * sinX;     // Изменен знак на МИНУС
	    float z1 = ry * sinX + rz * cosX;     // Изменен знак на ПЛЮС
	    ry = y1;
	    rz = z1;        

	    return new Point3D(rx, ry, rz);
	}

}
