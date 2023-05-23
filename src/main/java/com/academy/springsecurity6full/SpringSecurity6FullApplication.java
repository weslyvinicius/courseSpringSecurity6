package com.academy.springsecurity6full;


import com.academy.springsecurity6full.repository.AuthorityEnum;
import com.academy.springsecurity6full.repository.UserAuthorities;
import com.academy.springsecurity6full.repository.UserEntity;
import com.academy.springsecurity6full.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Arrays;

@SpringBootApplication
@EnableJpaRepositories
public class SpringSecurity6FullApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurity6FullApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner( UserRepository userRepository){
		return args -> {

			UserAuthorities roleEmployee = new UserAuthorities();
			roleEmployee.setAuthority( AuthorityEnum.ROLE_EMPLOYEE );

			UserAuthorities roleManager = new UserAuthorities();
			roleManager.setAuthority( AuthorityEnum.ROLE_MANAGER );

			UserAuthorities roleAdmin = new UserAuthorities();
			roleAdmin.setAuthority( AuthorityEnum.ROLE_ADMIN );

			UserEntity john = new UserEntity();
			john.setId( 1L );
			john.setName( "john" );
			john.setPassword( "j123456" );
			john.setAuthorities( Arrays.asList( roleEmployee ) );

			UserEntity mary = new UserEntity();
			mary.setId( 2L );
			mary.setName( "mary" );
			mary.setPassword( "m123456" );
			mary.setAuthorities( Arrays.asList( roleEmployee, roleManager ) );

			UserEntity susan = new UserEntity();
			susan.setId( 3L );
			susan.setName( "susan" );
			susan.setPassword( "s123456" );
			susan.setAuthorities( Arrays.asList( roleEmployee, roleManager, roleAdmin ) );

			userRepository.save( john );
			userRepository.save( mary );
			userRepository.save( susan );

		};
	}

    // User Encode for password
	/*
	@Bean
	CommandLineRunner commandLineRunner( UserRepository userRepository, PasswordEncoder passwordEncoder ){
		return args -> {

			UserEntity john = new UserEntity();
			john.setId( 1L );
			john.setName( "john" );
			john.setPassword( passwordEncoder.encode("j123456" ) );

			UserEntity mary = new UserEntity();
			mary.setId( 2L );
			mary.setName( "mary" );
			mary.setPassword( passwordEncoder.encode("m123456" ));

			UserEntity susan = new UserEntity();
			susan.setId( 3L );
			susan.setName( "susan" );
			susan.setPassword( passwordEncoder.encode("s123456" ));

			userRepository.save( john );
			userRepository.save( mary );
			userRepository.save( susan );

		};
	}
	*/


	/*

    // New for de save password encode, not bad use
	@Bean
	CommandLineRunner commandLineRunner( UserRepository userRepository ){
		return args -> {

			UserEntity john = new UserEntity();
			john.setId( 1L );
			john.setName( "john" );
			john.setPassword( "{noop}j123456" );

			UserEntity mary = new UserEntity();
			mary.setId( 2L );
			mary.setName( "mary" );
			mary.setPassword( "{noop}m123456");

			UserEntity susan = new UserEntity();
			susan.setId( 3L );
			susan.setName( "susan" );
			susan.setPassword( "{noop}s123456");

			userRepository.save( john );
			userRepository.save( mary );
			userRepository.save( susan );

		};
	}
	*/

}
