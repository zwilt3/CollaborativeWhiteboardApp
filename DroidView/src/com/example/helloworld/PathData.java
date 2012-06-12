package com.example.helloworld;

import java.io.Serializable;
import java.util.ArrayList;
import android.graphics.Path;

public class PathData implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Float> xPoints;
	private ArrayList<Float> yPoints;
	
	public PathData(){
		this(new ArrayList<Float>(), new ArrayList<Float>());
	}
	
	public PathData(ArrayList<Float> xPoints, ArrayList<Float> yPoints){
		this.xPoints = xPoints;
		this.yPoints = yPoints;
	}
	
	public void addPoint(float x, float y){
		xPoints.add(x);
		yPoints.add(y);
	}
	
	public void removePoint(int index){
		xPoints.remove(index);
		yPoints.remove(index);
	}
	
	public ArrayList<Float> getXPoints(){
		return xPoints;
	}
	
	public ArrayList<Float> getYPoints(){
		return yPoints;
	}
	
	public Path constructPath(){
		Path p = new Path();
		if (xPoints.size() == 0){
			return p;
		}
		p.moveTo(xPoints.get(0), yPoints.get(0));
		for (int i = 1; i < xPoints.size(); i++){
			p.lineTo(xPoints.get(i), yPoints.get(i));
		}
		return p;
	}
}
