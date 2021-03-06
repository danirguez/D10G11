
package services;

import java.util.ArrayList;
import java.util.Collection;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;

import repositories.UserRepository;
import security.Authority;
import security.LoginService;
import security.UserAccount;
import domain.Article;
import domain.User;
import forms.UserForm;

@Service
@Transactional
public class UserService {

	// Managed repository -----------------------------------------------------

	@Autowired
	private UserRepository	userRepository;
	
	// Supporting services ----------------------------------------------------

	@Autowired
	private Validator validator;

	// Constructor ------------------------------------------------------------

	public UserService() {
		super();
	}

	// Simple CRUD methods ----------------------------------------------------

	public User create() {
		User result;
		result = new User();
		final UserAccount userAccount = new UserAccount();
		final Authority authority = new Authority();
		authority.setAuthority(Authority.USER);
		userAccount.addAuthority(authority);
		result.setUserAccount(userAccount);
		return result;
	}

	public Collection<User> findAll() {
		Collection<User> result;
		result = this.userRepository.findAll();
		Assert.notNull(result);
		return result;
	}

	public User findOne(final int userId) {
		User result;
		result = this.userRepository.findOne(userId);
		return result;
	}

	public User save(final User user) {
		User result = user;
		Assert.notNull(user);
		if (user.getId() == 0) {
			Class<?> caught;
			caught = null;
			try {
				LoginService.getPrincipal();
			} catch (final Throwable oops) {
				caught = oops.getClass();
			}
			this.checkExceptions(IllegalArgumentException.class, caught);
		}
		if (user.getId() == 0) {
			String pass = user.getUserAccount().getPassword();
			final Md5PasswordEncoder code = new Md5PasswordEncoder();
			pass = code.encodePassword(pass, null);
			user.getUserAccount().setPassword(pass);
		}
		result = this.userRepository.save(result);
		return result;
	}

	public void delete(final User user) {
		Assert.notNull(user);
		Assert.isTrue(user.getId() != 0);
		this.userRepository.delete(user);
	}

	// Other business method --------------------------------------------------
	
	public void follow(int userId){
		User actual;
		actual = this.findByPrincipal();
		
		User follow;
		follow = this.findOne(userId);
		
		if (actual != follow) {
			Collection<User> following;
			following = actual.getFollowing();
			
			Collection<User> followers;
			followers = follow.getFollowers();
			
			if (followers.contains(actual)) {
				follow.setFollowers(followers);
				actual.setFollowing(following);
				
			} else {
				following.add(follow);
				followers.add(actual);
				
				follow.setFollowers(followers);
				actual.setFollowing(following);
			}
		}
	}
	
	public void unfollow(int userId){
		User actual;
		actual = this.findByPrincipal();
		
		User unfollow;
		unfollow = this.findOne(userId);
		
		Collection<User> following;
		following = actual.getFollowing();
		
		Collection<User> followers;
		followers = unfollow.getFollowers();
		
		following.remove(unfollow);
		followers.remove(actual);
		
		unfollow.setFollowers(followers);
		actual.setFollowing(following);
	}

	public User findByPrincipal() {
		User e;
		UserAccount userAccount;
		userAccount = LoginService.getPrincipal();
		Assert.notNull(userAccount);
		e = this.userRepository.findByPrincipal(userAccount.getId());
		return e;
	}

	public boolean checkUserLogged() {
		boolean result = false;
		UserAccount userAccount;
		userAccount = LoginService.getPrincipal();
		Assert.notNull(userAccount);
		final Collection<Authority> authority = userAccount.getAuthorities();
		Assert.notNull(authority);
		final Authority res = new Authority();
		res.setAuthority("USER");
		if (authority.contains(res))
			result = true;
		return result;
	}

	public void checkAuthority() {
		UserAccount userAccount;
		userAccount = LoginService.getPrincipal();
		Assert.notNull(userAccount);
		final Collection<Authority> authority = userAccount.getAuthorities();
		Assert.notNull(authority);
		final Authority res = new Authority();
		res.setAuthority("USER");
		Assert.isTrue(authority.contains(res));
	}

	public UserForm reconstruct(final UserForm userForm, final BindingResult binding) {
		User res;
		UserForm userFinal = null;
		res = userForm.getUser();
		if (res.getId() == 0) {
			Collection<Article> articles;
			Collection<User> following;
			Collection<User> followers;
			UserAccount userAccount;
			Authority authority;
			userAccount = userForm.getUser().getUserAccount();
			authority = new Authority();
			articles = new ArrayList<Article>();
			following = new ArrayList<User>();
			followers = new ArrayList<User>();
			userForm.getUser().setUserAccount(userAccount);
			authority.setAuthority(Authority.USER);
			userAccount.addAuthority(authority);
			userForm.getUser().setArticles(articles);
			userForm.getUser().setFollowing(following);
			userForm.getUser().setFollowers(followers);
			userFinal = userForm;
		} else {
			res = this.userRepository.findOne(userForm.getUser().getId());
			userForm.getUser().setId(res.getId());
			userForm.getUser().setVersion(res.getVersion());
			userForm.getUser().setUserAccount(res.getUserAccount());
			userForm.getUser().setArticles(res.getArticles());
			userForm.getUser().setFollowing(res.getFollowing());
			userForm.getUser().setFollowers(res.getFollowers());
			userFinal = userForm;
		}
		this.validator.validate(userFinal, binding);
		return userFinal;
	}

	public User reconstruct(final User user, final BindingResult binding) {
		User res;
		User userFinal;
		if (user.getId() == 0) {
			UserAccount userAccount;
			Authority authority;
			userAccount = user.getUserAccount();
			user.setUserAccount(userAccount);
			authority = new Authority();
			authority.setAuthority(Authority.USER);
			userAccount.addAuthority(authority);
			String password = "";
			password = user.getUserAccount().getPassword();
			user.getUserAccount().setPassword(password);
			userFinal = user;
		} else {
			res = this.userRepository.findOne(user.getId());
			user.setId(res.getId());
			user.setVersion(res.getVersion());
			user.setUserAccount(res.getUserAccount());
			user.getUserAccount().setPassword(user.getUserAccount().getPassword());
			user.getUserAccount().setAuthorities(user.getUserAccount().getAuthorities());
			userFinal = user;
		}
		this.validator.validate(userFinal, binding);
		return userFinal;
	}

	public void flush() {
		this.userRepository.flush();
	}
	protected void checkExceptions(final Class<?> expected, final Class<?> caught) {
		if (expected != null && caught == null)
			throw new RuntimeException(expected.getName() + " was expected");
		else if (expected == null && caught != null)
			throw new RuntimeException(caught.getName() + " was unexpected");
		else if (expected != null && caught != null && !expected.equals(caught))
			throw new RuntimeException(expected.getName() + " was expected, but " + caught.getName() + " was thrown");
	}
}
