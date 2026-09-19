package org.tuvaya.engine.types;

public class Triangle {
	
	public Triangle(int v0, int v1, int v2, int n0, int n1, int n2, int t0, int t1, int t2) {
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		this.n0 = n0;
		this.n1 = n1;
		this.n2 = n2;
		this.t0 = t0;
		this.t1 = t1;
		this.t2 = t2;
	}        
	public Triangle(Triangle tri) {
		this.v0 = tri.v0;
		this.v1 = tri.v1;
		this.v2 = tri.v2;
		this.n0 = tri.n0;
		this.n1 = tri.n1;
		this.n2 = tri.n2;
		this.t0 = tri.t0;
		this.t1 = tri.t1;
		this.t2 = tri.t2;
		this.color = tri.color;
	}
	public void add(int vis, int vtis, int vnis) {
		v0 += vis; v1 += vis; v2 += vis;
		t0 += vtis; t1 += vtis; t2 += vtis;
		n0 += vnis; n1 += vnis; n2 += vnis;
	}
	public int v0,v1,v2;
	public int n0,n1,n2;
	public int t0,t1,t2;
	
	public int color;
	
}

