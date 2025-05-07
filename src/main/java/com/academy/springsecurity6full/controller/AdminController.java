package com.academy.springsecurity6full.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

	@GetMapping
	public String getAllOfEmployees() {
		return "Read all admin employees";
	}

	@GetMapping("/{employeeId}")
	public String getAdminEmployee(@PathVariable String employeeId) {
		return "Read Admin Employee: " + employeeId;
	}

	@PostMapping
	public String saveAdminEmployee() {
		return "Create Admin Employee";
	}

	@PutMapping("/{employeeId}")
	public String updateAdminEmployee(@PathVariable String employeeId) {
		return "Update Admin Employee: " + employeeId;
	}

	@DeleteMapping("/{employeeId}")
	public String deleteAdminEmployee(@PathVariable String employeeId) {
		return "Delete Admin Employee: " + employeeId;
	}

}
