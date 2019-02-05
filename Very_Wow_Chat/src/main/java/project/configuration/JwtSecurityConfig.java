
package project.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import static java.util.Arrays.asList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import project.security.JwtAuthenticationEntryPoint;
import project.security.JwtAuthenticationProvider;
import project.security.JwtAuthenticationTokenFilter;
import project.security.JwtSuccessHandler;

import java.util.Collections;

/**
 * This is our authentication flow, i.e how the server should react to a request
 * from client.
 * 
 * @author Róman(ror9@hi.is)
 */
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
@Configuration
public class JwtSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private JwtAuthenticationProvider authenticationProvider; // our authenticator

	@Autowired
	private JwtAuthenticationEntryPoint entryPoint; // what happens if the authentication fails

	@Bean
	public AuthenticationManager authenticationManager() {
		return new ProviderManager(Collections.singletonList(authenticationProvider));
	}

	@Bean
	public JwtAuthenticationTokenFilter authenticationTokenFilter() {
		JwtAuthenticationTokenFilter filter = new JwtAuthenticationTokenFilter();
		filter.setAuthenticationManager(authenticationManager());
		filter.setAuthenticationSuccessHandler(new JwtSuccessHandler());
		return filter;
	}

	/**
	 * here we define the flow, on how a request from the client should be handled.
	 * we start by disabling csrf and add a custom cors fliter to prevent cors
	 * header errors next we add the entryPoint for redirecting the request. we
	 * define what methods are allowed and what methods need authentication all
	 * other methods outside the scope are handled by spring back magic. in a
	 * nutshell the request flow looks like : -> Disable csrf -> disable cors ->
	 * apply middleware authentication if needed -> (2 variants can happen here) (1)
	 * -> authentication not needed/successful -> send the request forward to be
	 * handaled by the controller (2) -> authentication failed -> send request to be
	 * handled by the error entryPoint (dosent allow the request to be handled by
	 * the request controller)
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().cors().and().exceptionHandling().authenticationEntryPoint(entryPoint).and()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and().authorizeRequests()
				.antMatchers("/auth/**").authenticated() // if user tries to access anything under /auth/+++ he needs to
															// be authenticated
				.antMatchers(HttpMethod.POST, "/login", "/register").permitAll() // POST Requests on login and register
																					// are allowed
				.antMatchers(HttpMethod.PUT, "/validation/**").permitAll() // PUT requests on validation are allowed
				.anyRequest().permitAll(); // any other request is allowed in hopes Spring black magic will handle it
		// you can use addFIlterBefore to squeze your own custom filter in to make the
		// code abit more readable
		http.addFilterBefore(authenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);
		http.headers().cacheControl();
	}

	/**
	 * This is a custom CORS filter that allowed all of the requests we use from the
	 * localhost client running on port 3000
	 * 
	 * BE WARNED IF YOU WANT TO USE THIS FILTER YOU HAVE TO ADD .cors() IN configure
	 * AND THIS FUNCTION HAS TO HAVE "Bean" ANNOTATION AND MUST RETURN
	 * "CorsConfigurationSource" AND MUST BE NAMED "corsConfigurationSource" OR ELSE
	 * .cors() WONT USE IT !!!!!
	 * 
	 * @return new source
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		final CorsConfiguration configuration = new CorsConfiguration();
		// define who is allowed
		configuration.setAllowedOrigins(asList("http://localhost:3000"));
		// define methods that we allow
		configuration.setAllowedMethods(asList("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH"));
		configuration.setAllowCredentials(true);
		// define headers that are allowed (we use Authorization to pass JWT tokens)
		configuration.setAllowedHeaders(asList("Authorization", "Cache-Control", "Content-Type"));
		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		// we allow cors on the entire app. we can limit it later if we want
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}
