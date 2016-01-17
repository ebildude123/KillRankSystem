package com.weidizhang.killranksystem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class PlayerPointsDB {
	HashMap<String, Integer> pointsMap = new HashMap<String, Integer>();
	String file;
	
	@SuppressWarnings("unchecked")
	public PlayerPointsDB(String filePath) {
		file = filePath;
		
		try {
			FileInputStream inStream = new FileInputStream(file);
			ObjectInputStream objStream = new ObjectInputStream(inStream);
			pointsMap = (HashMap<String, Integer>) objStream.readObject();
			objStream.close();
			inStream.close();
		}
		catch (IOException | ClassNotFoundException e) {}
	}
	
	public int getPoints(String p) {
		String name = p.toLowerCase();
		if (pointsMap.containsKey(name)) {
			return pointsMap.get(name);
		}
		return 0;
	}
	
	public void setPoints(String p, int amount) {
		String name = p.toLowerCase();
		pointsMap.put(name, amount);
		save();
	}
	
	public void addPoints(String p, int amount) {
		setPoints(p, getPoints(p) + amount);
	}
	
	public void removePoints(String p, int amount) {
		setPoints(p, getPoints(p) - amount);
	}
	
	public void save() {
		try {
			FileOutputStream outStream = new FileOutputStream(file);
			ObjectOutputStream objStream = new ObjectOutputStream(outStream);
			objStream.writeObject(pointsMap);
			objStream.close();
			outStream.close();
		}
		catch (IOException e) {}
	}
}