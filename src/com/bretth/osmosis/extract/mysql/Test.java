package com.bretth.osmosis.extract.mysql;

import com.bretth.osmosis.core.mysql.common.FixedPrecisionCoordinateConvertor;
import com.bretth.osmosis.core.mysql.common.TileCalculator;


/**
 * Simple test program that is randomly updated to test current features.
 * 
 * @author Brett Henderson
 */
public class Test {
	
	private static FixedPrecisionCoordinateConvertor convertor = new FixedPrecisionCoordinateConvertor();
	private static TileCalculator calculator = new TileCalculator();
	
	/**
	 * Entry point to the application.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) {
		testDoubleFixed(0);
		testDoubleFixed(1);
		testDoubleFixed(-180);
		testDoubleFixed(180);
		testDoubleFixed(1.1111111111);
		
		testTile(-90, -180);
		testTile(0, 0);
		testTile(90, 180);
		testTile(89.997, 179.99);
		testTile(-89.997, -179.99);
		testTile(-45, -90);
		testTile(-1, -2);
	}
	
	private static void testDoubleFixed(double inputDoubleCoordinate) {
		int fixedCoordinate;
		double doubleCoordinate;
		
		fixedCoordinate = convertor.convertToFixed(inputDoubleCoordinate);
		doubleCoordinate = convertor.convertToDouble(fixedCoordinate);
		
		System.out.println("double: " + inputDoubleCoordinate + " fixed: " + fixedCoordinate + " double: " + doubleCoordinate);
	}
	
	private static void testTile(double latitude, double longitude) {
		int tile;
		
		tile = calculator.calculateTile(latitude, longitude);
		
		System.out.println("latitude: " + latitude + " longitude: " + longitude + " tile: " + tile);
	}
}
