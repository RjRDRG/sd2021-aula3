package sd2021.aula3.server.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;
import sd2021.aula3.api.User;
import sd2021.aula3.api.service.RestUsers;

@Singleton
public class UsersResource implements RestUsers {

	private final Map<String,User> users = new HashMap<>();

	private static Logger Log = Logger.getLogger(UsersResource.class.getName());

	public UsersResource() {
	}

	@Override
	public String createUser(User user) {
		Log.info("createUser : " + user);

		// Check if user is valid, if not return HTTP CONFLICT (409)
		if(user.getUserId() == null || user.getPassword() == null || user.getFullName() == null || 
				user.getEmail() == null) {
			Log.info("User object invalid.");
			throw new WebApplicationException( Status.CONFLICT );
		}

		synchronized ( this ) {

			// Check if userId does not exist exists, if not return HTTP CONFLICT (409)
			if( users.containsKey(user.getUserId())) {
				Log.info("User already exists.");
				throw new WebApplicationException( Status.CONFLICT );
			}

			//Add the user to the map of users
			users.put(user.getUserId(), user);

			return user.getUserId();
		}
	}


	@Override
	public User getUser(String userId, String password) {
		Log.info("getUser : user = " + userId + "; pwd = " + password);

		// Check if user is valid, if not return HTTP CONFLICT (409)
		if(userId == null || password == null) {
			Log.info("UserId or passwrod null.");
			throw new WebApplicationException( Status.CONFLICT );
		}

		User user = users.get(userId);

		// Check if user exists 
		if( user == null ) {
			Log.info("User does not exist.");
			throw new WebApplicationException( Status.NOT_FOUND );
		}

		//Check if the password is correct
		if( !user.getPassword().equals( password)) {
			Log.info("Password is incorrect.");
			throw new WebApplicationException( Status.FORBIDDEN );
		}

		return user;
	}


	@Override
	public User updateUser(String userId, String password, User user) {
		Log.info("updateUser : user = " + userId + "; pwd = " + password + " ; user = " + user);

		// Check if user is valid, if not return HTTP CONFLICT (409)
		if(userId == null || password == null) {
			Log.info("UserId or passwrod null.");
			throw new WebApplicationException( Status.CONFLICT );
		}

		synchronized ( this ) {
			User oldUser = users.get(userId);

			// Check if user exists
			if( oldUser == null ) {
				Log.info("User does not exist.");
				throw new WebApplicationException( Status.NOT_FOUND );
			}

			//Check if the password is correct
			if( !oldUser.getPassword().equals( password)) {
				Log.info("Password is incorrect.");
				throw new WebApplicationException( Status.FORBIDDEN );
			}

			users.put(userId, new User(userId, user.getFullName(), user.getEmail(), user.getPassword()));

			return oldUser;
		}
	}


	@Override
	public User deleteUser(String userId, String password) {
		Log.info("deleteUser : user = " + userId + "; pwd = " + password);

		// Check if user is valid, if not return HTTP CONFLICT (409)
		if(userId == null || password == null) {
			Log.info("UserId or password null.");
			throw new WebApplicationException( Status.CONFLICT );
		}

		synchronized ( this ) {
			User user = users.get(userId);

			// Check if user exists
			if (user == null) {
				Log.info("User does not exist.");
				throw new WebApplicationException(Status.NOT_FOUND);
			}

			//Check if the password is correct
			if (!user.getPassword().equals(password)) {
				Log.info("Password is incorrect.");
				throw new WebApplicationException(Status.FORBIDDEN);
			}

			return users.remove(userId);
		}
	}


	@Override
	public List<User> searchUsers(String pattern) {
		Log.info("searchUsers : pattern = " + pattern);

		if(users.isEmpty()) {
			return new ArrayList<>();
		}

		if( pattern == null || pattern.isEmpty() ) {
			return new ArrayList<>(users.values());
		}

		return users.values().stream().filter(u -> u.getFullName().contains(pattern)).collect(Collectors.toList());
	}

}
