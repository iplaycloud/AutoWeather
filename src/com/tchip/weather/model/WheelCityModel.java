package com.tchip.weather.model;

import java.util.List;

public class WheelCityModel {
	private String name;
	private List<WheelDistrictModel> districtList;
	
	public WheelCityModel() {
		super();
	}

	public WheelCityModel(String name, List<WheelDistrictModel> districtList) {
		super();
		this.name = name;
		this.districtList = districtList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<WheelDistrictModel> getDistrictList() {
		return districtList;
	}

	public void setDistrictList(List<WheelDistrictModel> districtList) {
		this.districtList = districtList;
	}

	@Override
	public String toString() {
		return "CityModel [name=" + name + ", districtList=" + districtList
				+ "]";
	}
	
}
