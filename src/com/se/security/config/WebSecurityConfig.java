package com.se.security.config;


import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = false)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{	
	@Autowired
	DataSource dataSource;
	
	@Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication().dataSource(dataSource)
    	.usersByUsernameQuery(
			"select USERNAME, PASSWORD, 'true' as enabled "
			+ " from [ACCOUNT]"
			+ " where USERNAME=?")
		.authoritiesByUsernameQuery(
			"select USERNAME, ROLE"
			+ " from [ACCOUNT] "
			+ "where USERNAME=?")
		.rolePrefix("ROLE_");
		auth.inMemoryAuthentication().withUser("Mark").password("m1234").roles("manager");
    }
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http
	    	.authorizeRequests()
	    		.antMatchers("/").permitAll()
	    		.antMatchers("/register").anonymous()
	    		.antMatchers("/login").permitAll()
	    		.antMatchers("/user").hasAnyRole("customer", "manager")
	    		.antMatchers("/place").hasAnyRole("customer")
	    		.antMatchers("/place/manager").hasAnyRole("manager")
	    		.antMatchers("/place/create").hasAnyRole("customer")
	    		.antMatchers("/place/createForm").hasAnyRole("customer")
	    		.antMatchers("/place/delete").hasAnyRole("manager")
	    		.antMatchers("/place/showAll").hasAnyRole("manager")
	    		.antMatchers("/place/accept").hasAnyRole("manager")
	    		.antMatchers("/place/reject").hasAnyRole("manager")
	    		.antMatchers("/practice").hasAnyRole("customer")
	    		.antMatchers("/practice/create").hasAnyRole("customer")
	    		.antMatchers("/practice/createForm").hasAnyRole("customer")
	    		.antMatchers("/practice/delete").hasAnyRole("customer", "manager")
	    		.antMatchers("/security/**").hasAnyRole("manager")	    				
		        .antMatchers("/close").hasAnyRole("customer")
		        .anyRequest().permitAll()
		        .and()
		        .formLogin()
		        .loginPage("/login").permitAll()
		        .defaultSuccessUrl("/user")
		        .failureUrl("/loginError")
		        .and()
		   .exceptionHandling().accessDeniedPage("/accessError");
		        
		http.logout()
			.logoutUrl("/logout")
			.deleteCookies("JSESSIONID")
			.logoutSuccessUrl("/");
	}
}
