package com.yy.common.hostinfo.enums;

/**
 * ISP 枚举 ，涵盖ISP名称、类型ID和优先级
 *
 */
public enum IspType{
	
	INTRANET("INTRANET",32768,0,512),CTL("CTL",1,1,256), CNC("CNC",2,2,256), MOB("MOB",32,3,256);
	
	IspType(String ispName, int ispid, int level, int weight){
		this.ispName = ispName;
		this.setIspid(ispid);
		this.setLevel(level);
		this.setWeight(weight);
	}
	
	//ISP名称编码
	private String ispName;
	//ISP ID(S2s中登记的IP key)
	private int ispid;
	//路由时选取优先级
	private int level;
	//路由时附加的权重
	private int weight;

	public String getIspName() {
		return ispName;
	}
	public void setIspName(String ispName) {
		this.ispName = ispName;
	}
	
	public int getIspid() {
		return ispid;
	}
	public void setIspid(int ispid) {
		this.ispid = ispid;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	/**
	 * @return the weight
	 */
	public int getWeight() {
		return weight;
	}
	/**
	 * @param weight the weight to set
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}
	
    public static IspType create(int ispid){
        switch (ispid){
            case 1:
            	return CTL;
            case 2:
            	return CNC;
            case 32:
            	return MOB;
            case 32768:
            	return INTRANET;
        }
        return  CTL;
    }
    
    public static IspType create(String ispName){
    	if(ispName == null || ispName.isEmpty())
    		return null;
    	
        switch (ispName.toUpperCase()){
            case "CTL":
            	return CTL;
            case "CNC":
            	return CNC;
            case "MOB":
            	return MOB;
            case "INTRANET":
            	return INTRANET;
        }
        return  CTL;
    }
	
}
