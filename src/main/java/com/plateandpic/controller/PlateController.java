package com.plateandpic.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.plateandpic.dao.PlateDao;
import com.plateandpic.dao.RestaurantDao;
import com.plateandpic.exceptions.PlateException;
import com.plateandpic.exceptions.RestaurantNotFoundException;
import com.plateandpic.models.Plate;
import com.plateandpic.models.Restaurant;

@RestController
@RequestMapping("/plate")
public class PlateController {
	
	private static final Logger log = LoggerFactory.getLogger(PlateController.class);
	
	@Autowired
	private PlateDao plateDao;
	
	@Autowired
	private RestaurantDao restaurantDao;
	
	/**
     * POST /savePlate  --> Save new Plate and return it
     */
	@RequestMapping(value = "/save", method = RequestMethod.POST)
	@ResponseBody
	public Plate save(HttpServletRequest request, HttpServletResponse response,
			@RequestBody Plate plate){
		  
		Restaurant restaurant = null;
		Plate savedPlate = null;
		
		try{
			
			restaurant = restaurantDao.findOne(plate.getRestaurant().getRestaurantId());
			
			plate.setRestaurant(restaurant);
			
			savedPlate = plateDao.save(plate);
			
			if(savedPlate == null){
				throw new PlateException("Plate not saved: " + plate.toString());
			}
			
			response.setStatus(HttpServletResponse.SC_OK);
			  
		} catch(PlateException e){
			log.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			plate = null;
		}
		  
		return savedPlate;
		 
	}
	
	/**
     * GET /getPlatesByRestaurant --> get plates by restaurant
     */
	@RequestMapping(value = "/getPlatesByRestaurant", method = RequestMethod.GET)
	@ResponseBody
	public List<Plate> getPlatesByRestaurant(HttpServletRequest request, HttpServletResponse response,
			@RequestParam Long restaurantId){
		  
		Restaurant restaurant = null;
		List<Plate> plates = null;
		
		try{
			
			restaurant = restaurantDao.findOne(restaurantId);
			
			if(restaurant == null){
				throw new RestaurantNotFoundException("Restaurant not found with ID: " + restaurantId);
			}
			
			plates = plateDao.findByRestaurant(restaurant);
			
			if(plates == null){
				throw new PlateException("Plates not found for restaurantId: " + restaurantId);
			}
			
			response.setStatus(HttpServletResponse.SC_OK);
			  
		} catch(RestaurantNotFoundException e){
			log.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			plates = null;
		} catch(PlateException e){
			log.error(e.getMessage());
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			plates = null;
		}
		  
		return plates;
		 
	}
	
}