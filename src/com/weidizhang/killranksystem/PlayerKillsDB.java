package com.weidizhang.killranksystem;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class PlayerKillsDB {
	HashMap<String, HashMap<String, Long>> killsMap = new HashMap<String, HashMap<String, Long>>();
	String file;
	
	@SuppressWarnings("unchecked")
	public PlayerKillsDB(String filePath) {
		file = filePath;
		
		try {
			FileInputStream inStream = new FileInputStream(file);
			ObjectInputStream objStream = new ObjectInputStream(inStream);
			killsMap = (HashMap<String, HashMap<String, Long>>) objStream.readObject();
			objStream.close();
			inStream.close();
		}
		catch (IOException e) {}
		catch (ClassNotFoundException e) {}
	}
	
	public long getKillTime(String killer, String died) {
		if (killsMap.containsKey(killer)) {
			Long killTime = killsMap.get(killer).get(died);
			if (killTime == null) {
				return 0;
			}
			return killTime;
		}
		return 0;
	}
	
	public void setKillTime(String killer, String died, long time) {
		HashMap<String, Long> killInfo = new HashMap<String, Long>();
		if (killsMap.containsKey(killer)) {
			killInfo = killsMap.get(killer);
		}
		killInfo.put(died, time);
		killsMap.put(killer, killInfo);
		save();
	}
	
	public void save() {
		try {
			FileOutputStream outStream = new FileOutputStream(file);
			ObjectOutputStream objStream = new ObjectOutputStream(outStream);
			objStream.writeObject(killsMap);
			objStream.close();
			outStream.close();
		}
		catch (IOException e) {}
	}
}