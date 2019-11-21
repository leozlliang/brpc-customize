package com.yy.common.hostinfo.enums;

/**
 * ROUTER Filter 
 * room isp intranet set
 *
 */
public enum RouterFilterType{
	
	ROOM("room"),ISP("isp"), INTRANET("intranet"), SET("set");
	
	RouterFilterType(String name){
		this.setName(name);
	}
	
	//Router filter name
	private String name;
   
    public static RouterFilterType create(String name){
    	if(name == null || name.isEmpty())
    		return null;
    	
        switch (name.toLowerCase()){
            case "room":
            	return ROOM;
            case "isp":
            	return ISP;
            case "set":
            	return SET;
            case "intranet":
            	return INTRANET;
        }
        return  null;
    }

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
}
