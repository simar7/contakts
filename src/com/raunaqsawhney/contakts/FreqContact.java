package com.raunaqsawhney.contakts;

public class FreqContact {
	
	private String name;
	private Integer count;
	private String url;
	private String timesContacted;
	private String id;

	//Setters
	public void setName(String name) {
	    this.name = name;
	}
	
	public void setCount(Integer count) {
	    this.count = count;
	}
	
	public void setURL(String urlImg) {
		this.url = urlImg;
	}
	
	public void setTimesContacted(String timesContacted) {
		this.timesContacted = timesContacted;
	}
	
	public void setID(String id) {
		this.id = id;;
	}
	
	// Getters
    public String getName() {
        return name;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public String getURL() {
        return url;
    }
    
    public String getTimesContacted() {
        return timesContacted;
    }
    
    public String getID() {
    	return id;
    }

}
