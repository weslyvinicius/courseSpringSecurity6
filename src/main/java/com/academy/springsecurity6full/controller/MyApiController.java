package com.academy.springsecurity6full.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyApiController {


	@GetMapping("/myfree")
	public String myFree(){

		return "Acessando my endpoint free";
	}

	@GetMapping("/authenticate")
	public String myAuthenticate(){
		return "Acessando my endpoint authenticate";
	}



}
