package com.academy.springsecurity6full.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeesController {

	@GetMapping
	public String getAllOfEmployees(){
		return "Read all employees";
	}

	@GetMapping("/{employeeId}")
	public String getEmployee( @PathVariable String employeeId ){
		return "Read Employee";
	}

	@PostMapping
	public String saveEmployee(){
		return "Create Employee";
	}

	@PutMapping
	public String updateEmployee(){
		return "Update Employee";
	}

	@DeleteMapping("/{employeeId}")
	public String deleteEmployee( @PathVariable String employeeId ){
		return "Delete Employee";
	}


}
