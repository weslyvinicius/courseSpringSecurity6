package com.academy.springsecurity6full.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/employees")
/*
    Você também pode usar a anotação @PreAuthorize em um nível de classe e anotar toda a classe com @PreAuthorize.
    Nesse caso, todos os métodos de uma classe serão afetados pelo valor usado nessa anotação.
    A anotação de nível de método @PreAuthorize  tem uma prioridade mais alta e substituirá o valor usado no nível de classe.
    Vamos dar uma olhada no trecho de código a seguir.

    No exemplo de código acima, a anotação
    @PreAuthorize é usada em nível de classe e todos os métodos da classe são afetados por ela.
    Somente usuários na função “MANAGER” poderão acessar o terminal de serviço da Web /api/employees  .
    No entanto, GET-> /api/employees (@PreAuthorize("permitAll")) estará disponível para todos os usuários porque a anotação @PreAuthorize no nível do método substitui a anotação no nível da classe.
* */
// @PreAuthorize("hasRole('MANAGER')")
public class EmployeesController {


	@GetMapping
	@PreAuthorize("hasRole('EMPLOYEE')")
	//@PreAuthorize("permitAll")
	public String getAllOfEmployees(){
    	return "Read all employees";
	}

	@GetMapping("/{employeeId}")
	@PreAuthorize("hasRole('EMPLOYEE')")
	public String getEmployee( @PathVariable String employeeId ){
		return "Read Employee";
	}

	@PostMapping
	@PreAuthorize("hasRole('MANAGER')")
	public String saveEmployee(){
		return "Create Employee";
	}

	@PutMapping
	@PreAuthorize("hasRole('MANAGER')")
	public String updateEmployee(){
		return "Update Employee";
	}

	@DeleteMapping("/{employeeId}")
	@PreAuthorize("hasRole('ADMIN')")
	// @PreAuthorize("hasAuthority('DELETE_AUTHORITY')") --> YOU CAN USE AUTHORITIES
	public String deleteEmployee( @PathVariable String employeeId ){
		return "Delete Employee";
	}





}
