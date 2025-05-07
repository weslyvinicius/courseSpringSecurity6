package com.academy.springsecurity6full.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
public class EmployeesController {


	@GetMapping
	public String getAllOfEmployees() {
		return "Read all employees";
	}

	@GetMapping("/{employeeId}")
	public String getEmployee(@PathVariable String employeeId) {
		return "Read Employee: " + employeeId;
	}

	@PostMapping
	public String saveEmployee() {
		return "Create Employee";
	}

	@PutMapping("/{employeeId}")
	public String updateEmployee(@PathVariable String employeeId) {
		return "Update Employee: " + employeeId;
	}

	@DeleteMapping("/{employeeId}")
	public String deleteEmployee(@PathVariable String employeeId) {
		return "Delete Employee: " + employeeId;
	}

}
