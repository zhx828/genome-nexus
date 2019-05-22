package org.cbioportal.genome_nexus.web.security;

import com.google.api.core.ApiFuture;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.cbioportal.genome_nexus.web.security.model.FirebaseAuthenticationToken;
import org.cbioportal.genome_nexus.web.security.model.FirebaseUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class FirebaseAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	@Autowired
	private FirebaseAuth firebaseAuth;

	@Override
	public boolean supports(Class<?> authentication) {
		return (FirebaseAuthenticationToken.class.isAssignableFrom(authentication));
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
	}

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
			throws AuthenticationException {
		final FirebaseAuthenticationToken authenticationToken = (FirebaseAuthenticationToken) authentication;

        FirebaseToken firebaseToken = null;

        try {
            firebaseToken = firebaseAuth.verifyIdToken(authenticationToken.getToken());
            return new FirebaseUserDetails(firebaseToken.getEmail(), firebaseToken.getUid());
        } catch (FirebaseAuthException e) {
            throw new SessionAuthenticationException(e.getMessage());
        }
	}
}
