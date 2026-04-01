package backend.playsembly.web;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import backend.playsembly.domain.AppUser;
import backend.playsembly.domain.AppUserRepository;

/**
 * This class is used by spring security to authenticate and authorize user
 **/
@Service
public class UserDetailServiceImpl implements UserDetailsService  {
	
	private final AppUserRepository repository;
	
	// Constructor Injection
	public UserDetailServiceImpl(AppUserRepository appUserRepository) {
		this.repository = appUserRepository; 
	}
	
   @Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AppUser curruser = repository.findByUsername(username);
		if (curruser == null) throw new UsernameNotFoundException(username);
		return curruser; // palautetaan AppUser suoraan
	}
} 