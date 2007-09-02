package com.bretth.osmosis.extract.mysql;

import java.awt.geom.Area;
import java.awt.geom.Path2D;


/**
 * Simple test program that is randomly updated to test current features.
 * 
 * @author Brett Henderson
 */
public class Test {
	/**
	 * Entry point to the application.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		Path2D.Double path1;
		Area area1;
		Path2D.Double path2;
		Area area2;
		
		path1 = new Path2D.Double();
		path2 = new Path2D.Double();
		
		path1.moveTo(0, 0);
		path1.lineTo(1, 1);
		path1.lineTo(1, 0);
		path1.lineTo(0, 0);
		
		path2.moveTo(0.5, 0);
		path2.lineTo(0.5, 1);
		path2.lineTo(1, 1);
		path2.lineTo(1, 0);
		path2.lineTo(0.5, 0);
		
		area1 = new Area(path1);
		area2 = new Area(path2);
		
		System.out.println(area1.contains(0, 1));
		System.out.println(area1.contains(0.25, 0.125));
		System.out.println(area1.contains(0.75, 0.375));
		area1.subtract(area2);
		System.out.println(area1.contains(0.25, 0.125));
		System.out.println(area1.contains(0.75, 0.375));
	}
}
