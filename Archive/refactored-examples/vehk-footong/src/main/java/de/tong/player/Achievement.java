package de.tong.player;

/**
 * 
 * @author Eric Saier
 * 
 */

public class Achievement {
    private int id;
    private String name;
    private String descEN;
    private String descDE;
    private boolean unlocked;

    public Achievement(int id, String name, String descEN, String descDE) {
	this.id = id;
	this.name = name;
	this.descEN = descEN;
	this.descDE = descDE;
    }

    public String getName() {
	return name;
    }

    public void unlock() {
	this.unlocked = true;
    }

    public boolean isUnlocked() {
	return unlocked;
    }

    @Override
    public boolean equals(Object obj) {
	if (obj == null) {
	    return false;
	}
	if (this == obj) {
	    return true;
	}
	if (obj instanceof Achievement) {
	    Achievement other = (Achievement) obj;
	    if (this.getName().equals(other.getName())) {
		return true;
	    }
	}
	return false;
    }

}
