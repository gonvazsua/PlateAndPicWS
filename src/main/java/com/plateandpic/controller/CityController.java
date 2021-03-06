package com.plateandpic.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.plateandpic.dao.CityDao;
import com.plateandpic.factory.CityFactory;
import com.plateandpic.response.CityResponse;

/**
 * @author gonzalo
 *
 */
@RestController
@RequestMapping("/city")
public class CityController { 
	
	private static final Logger log = LoggerFactory.getLogger(CityController.class);
	
	@Autowired
	private CityFactory cityFactory;
	
	
	/**
	 * @param request
	 * @param response
	 * @param name
	 * @return
	 */
	@RequestMapping(value = "/findByName", method = RequestMethod.GET)
	@ResponseBody
	public List<CityResponse> getCityByName(HttpServletRequest request, HttpServletResponse response,
			@RequestParam String name){
		  
		List<CityResponse> cityResponse = null;
		
		if(name != null && !name.isEmpty()){
			
			cityResponse = cityFactory.findCitiesByName(name);
			
		} else {
			
			cityResponse = new ArrayList<CityResponse>();
			
		}
		
		return cityResponse;
		 
	}

}
