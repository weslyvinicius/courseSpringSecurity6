package com.academy.springsecurity6full.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyApiController {

	/*
	* Este projeto demostra como implementar o Spring Security 6 com o Spring Boot 3
	* utilizando basic authentication e form login.
	*
	* */

	@GetMapping("/myfree")
	public String myFree(){

		return "Acessando my endpoint free";
	}

	@GetMapping("/authenticate")
	public String myAuthenticate(){
		return "Acessando my endpoint authenticate";
	}



}
