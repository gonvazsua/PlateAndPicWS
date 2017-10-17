package com.plateandpic.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.plateandpic.constants.ConstantsProperties;
import com.plateandpic.constants.MessageConstants;
import com.plateandpic.dao.UserDao;
import com.plateandpic.exceptions.PasswordException;
import com.plateandpic.exceptions.PlateAndPicException;
import com.plateandpic.exceptions.UserException;
import com.plateandpic.models.User;
import com.plateandpic.response.UserResponse;
import com.plateandpic.security.JwtTokenUtil;
import com.plateandpic.utils.UpdatePasswordRequest;

/**
 * @author gonzalo
 *
 */
@Service
public class UserFactory {
	
	private static final Integer ROW_LIMIT = 30;
	private static final String QUERY_SORT = "name";
	
	@Autowired
	UserDao userDao;
	
	@Autowired
	private Environment env;
	
	@Autowired
	private JwtTokenUtil jwtTokenUtil;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	/**
	 * @param userFrom
	 * @param userTo
	 */
	public void copyFieldsFromPersonalDataChange(User userFrom, User userTo){
		
		userTo.setUsername(userFrom.getUsername());
		userTo.setFirstname(userFrom.getFirstname());
		userTo.setLastname(userFrom.getLastname());
		userTo.setTarget(userFrom.getTarget());
		
	}
	
	/**
	 * @param userFrom
	 * @param userTo
	 */
	public void copyFieldsFromEmailChange(User userFrom, User userTo){
		
		userTo.setEmail(userFrom.getEmail());
		
	}
	
	/**
	 * @param userId
	 * @return
	 * @throws PlateAndPicException 
	 * @throws IOException
	 */
	public UserResponse getUserResponse(Long userId) throws PlateAndPicException {
		
		UserResponse userResponse = null;
		User user = null;
		
		if(userId == null || userId == 0){
			throw new UsernameNotFoundException(MessageConstants.USER_USER_NOT_FOUND);
		}
		
		user = userDao.findOne(userId);
		
		if(user == null){
			throw new UsernameNotFoundException(MessageConstants.USER_USER_NOT_FOUND);
		}
		
		userResponse = buildUserResponse(user);
		
		return userResponse;
		
	}
	
	/**
	 * @param user
	 * @return
	 * @throws IOException
	 * @throws PlateAndPicException 
	 */
	public UserResponse buildUserResponse(User user) throws PlateAndPicException{
		
		UserResponse userResponse = null;
		String base64UserPicture = "";
		
		userResponse = new UserResponse(user);
		
		if(user.getPicture() != null && !"".equals(user.getPicture())){
			base64UserPicture = FileFactory.getBase64FromProfilePictureName(getProfilePicturePath(), user.getPicture());
			userResponse.setPicture(base64UserPicture);
		}
		
		return userResponse;
		
	}
	
	/**
	 * @param user
	 * @return
	 * @throws IOException
	 * @throws PlateAndPicException 
	 * 
	 * Build a List of UserResponse from User list
	 */
	public List<UserResponse> buildUserResponseList(List<User> users) throws PlateAndPicException{
		
		UserResponse userResponse = null;
		List<UserResponse> response = new ArrayList<UserResponse>();
		
		if(users == null || users.isEmpty()){
			return response;
		}
		
		for(User u : users){
			
			userResponse = buildUserResponse(u);
			response.add(userResponse);
			
		}
		
		return response;
		
	}
	
	/**
	 * @return
	 */
	public String getProfilePicturePath(){
		
		return env.getProperty(ConstantsProperties.USER_PROFILE_PICTURE_PATH);
		
	}
	
	/**
	 * @param token
	 * @return
	 * @throws UserException
	 */
	public User getUserFromToken(String token) throws UserException {
		
		Long userId = jwtTokenUtil.getUserIdFromToken(token);
		
		User user = userDao.findOne(userId);
		
		if(user == null){
			throw new UserException(MessageConstants.USER_USER_NOT_FOUND);
		}
		
		return user;
		
	}
	
	/**
	 * @param token
	 * @return
	 */
	public Long getUserIdFromToken(String token){
		
		return jwtTokenUtil.getUserIdFromToken(token);
		
	}
	
	/**
	 * @param username
	 * @return
	 * @throws UserException
	 */
	public User getUserByUsername(String username) throws UserException{
		
		User user = userDao.findByUsername(username);
		
		if(user == null){
			throw new UserException(MessageConstants.USER_USER_NOT_FOUND);
		}
		
		return user;
		
	}
	
	/**
	 * @param email
	 * @return
	 * @throws UserException
	 */
	public User getUserByEmail(String email) throws UserException{
		
		User user = userDao.findByEmail(email);
		
		if(user == null){
			throw new UserException(MessageConstants.USER_USER_NOT_FOUND);
		}
		
		return user;
		
	}
	
	/**
	 * @param oldUsername, newUsername
	 * @return
	 * @throws UserException
	 * 
	 * Check if username has changed and is unique in DB
	 */
	public void checkUsername(String oldUsername, String newUsername) throws UserException{
		
		if(!(oldUsername.equals(newUsername)) && (getUserByUsername(newUsername) != null)){
			
			throw new UserException(MessageConstants.USER_USERNAME_ALREADY_USED);
			
		}
		
	}
	
	/**
	 * @param oldEmail, newEmail
	 * @return
	 * @throws UserException
	 * 
	 * Check if email has changed and is unique in DB
	 */
	public void checkEmail(String oldEmail, String newEmail) throws UserException{
		
		if(!(oldEmail.equals(newEmail)) && (getUserByEmail(newEmail) != null)){
			
			throw new UserException(MessageConstants.USER_USERNAME_ALREADY_USED);
			
		}
		
	}
	
	/**
	 * @param user
	 * 
	 * Save user in DB
	 */
	public void saveUser(User user){
		
		userDao.save(user);
		
	}
	
	/**
	 * @param passwordRequest
	 * @param user
	 * @throws PasswordException
	 */
	public void validateLastPasswordAndUpdate(UpdatePasswordRequest passwordRequest, User user) throws PasswordException{
		
		validateLastPassword(passwordRequest, user);
		
		updateNewPassword(passwordRequest, user);
		
		saveUser(user);
		
	}
	
	/**
	 * @param passwordRequest
	 * @param user
	 * @throws PasswordException
	 */
	private void validateLastPassword(UpdatePasswordRequest passwordRequest, User user) throws PasswordException{
		
		String lastPassword = passwordEncoder.encode(passwordRequest.getLastPassword());
		
		if(!user.getPassword().equals(lastPassword)){
			throw new PasswordException(MessageConstants.PASSWORD_NOT_CORRECT);
		}
		
	}
	
	/**
	 * @param passwordRequest
	 * @param user
	 */
	private void updateNewPassword(UpdatePasswordRequest passwordRequest, User user){
		
		String newPassword = passwordEncoder.encode(passwordRequest.getNewPassword1());
		
		user.setPassword(newPassword);
		
	}
	
	/**
	 * @param keySearch
	 * @param page
	 * @return
	 * 
	 * Find users with name, lastname or username like the keysearch
	 * @throws PlateAndPicException 
	 */
	public List<UserResponse> findUsersByKeySearch(String keySearch, Integer page) throws PlateAndPicException{
		
		List<User> userList = null;
		List<UserResponse> userResponseList = null;
		Pageable pageable = null;
		
		//pageable = new PageRequest(page, ROW_LIMIT, Sort.Direction.DESC, QUERY_SORT);
		userList = userDao.findUserByKey(keySearch.trim());
		
		userResponseList = buildUserResponseList(userList);
		
		return userResponseList;
		
	}

}
