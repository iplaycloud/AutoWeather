package com.tchip.weather.model;

import java.util.List;

public class WheelProvinceModel {
	private String name;
	private List<WheelCityModel> cityList;
	
	public WheelProvinceModel() {
		super();
	}

	public WheelProvinceModel(String name, List<WheelCityModel> cityList) {
		super();
		this.name = name;
		this.cityList = cityList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<WheelCityModel> getCityList() {
		return cityList;
	}

	public void setCityList(List<WheelCityModel> cityList) {
		this.cityList = cityList;
	}

	@Override
	public String toString() {
		return "ProvinceModel [name=" + name + ", cityList=" + cityList + "]";
	}
	
}
