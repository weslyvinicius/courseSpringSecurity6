package com.academy.springsecurity6full;

import com.academy.springsecurity6full.repository.AuthorityEnum;
import com.academy.springsecurity6full.repository.UserEntity;
import com.academy.springsecurity6full.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableJpaRepositories
@Slf4j
public class SpringSecurity6FullApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringSecurity6FullApplication.class, args);
	}

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Bean
	CommandLineRunner commandLineRunner(UserRepository userRepository){
		return args -> {

			UserEntity john = new UserEntity();
			john.setId( 1L );
			john.setName( "john" );
			john.setPassword( passwordEncoder.encode("j123456") );
			john.createAuthorities( AuthorityEnum.ROLE_EMPLOYEE, AuthorityEnum.READ_EMPLOYEE );

			UserEntity mary = new UserEntity();
			mary.setId( 2L );
			mary.setName( "mary" );
			mary.setPassword(  passwordEncoder.encode("m123456")  );
			mary.createAuthorities( AuthorityEnum.ROLE_MANAGER,
					AuthorityEnum.READ_EMPLOYEE,
					AuthorityEnum.CREATE_EMPLOYEE,
					AuthorityEnum.UPDATE_EMPLOYEE,
					AuthorityEnum.READ_REPORT );

			UserEntity susan = new UserEntity();
			susan.setId( 3L );
			susan.setName( "susan" );
			susan.setPassword(  passwordEncoder.encode("s123456")  );
			susan.createAuthorities( AuthorityEnum.ROLE_ADMIN,
					AuthorityEnum.READ_EMPLOYEE,
					AuthorityEnum.CREATE_EMPLOYEE,
					AuthorityEnum.UPDATE_EMPLOYEE,
					AuthorityEnum.DELETE_EMPLOYEE,
					AuthorityEnum.READ_REPORT,
					AuthorityEnum.CREATE_REPORT,
					AuthorityEnum.UPDATE_REPORT,
					AuthorityEnum.DELETE_REPORT );

			userRepository.save( john );
			userRepository.save( mary );
			userRepository.save( susan );

			log.info("Users created: {}, {}, {}", john.getName(), mary.getName(), susan.getName());

		};
	}
}
