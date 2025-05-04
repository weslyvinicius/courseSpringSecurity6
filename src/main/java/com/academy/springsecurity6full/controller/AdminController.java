package com.academy.springsecurity6full.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
/*
    Você também pode usar a anotação @PreAuthorize em um nível de classe e anotar toda a classe com @PreAuthorize.
    Nesse caso, todos os métodos de uma classe serão afetados pelo valor usado nessa anotação.
    A anotação de nível de método @PreAuthorize  tem uma prioridade mais alta e substituirá o valor usado no nível de classe.
    Vamos dar uma olhada no trecho de código a seguir.

    No exemplo de código acima, a anotação
    @PreAuthorize é usada em nível de classe e todos os métodos da classe são afetados por ela.
    Somente usuários na função “ADMIN” poderão acessar o terminal de serviço da Web /api/admin  .
    No entanto, GET-> /api/admin (@PreAuthorize("permitAll")) estará disponível para todos os usuários porque a anotação @PreAuthorize no nível do método substitui a anotação no nível da classe.
* */
 @PreAuthorize("hasRole('ADMIN')")
public class AdminController {


	@GetMapping
	@PreAuthorize("permitAll")
	public String getAllOfEmployees(){
    	return "Read all admin employees";
	}

	@GetMapping("/{employeeId}")
	public String getAdminEmployee( @PathVariable String employeeId ){
		return "Read Admin Employee";
	}

	@PostMapping
	public String saveAdminEmployee(){
		return "Create Admin Employee";
	}

	@PutMapping
	public String updateAdminEmployee(){
		return "Update Admin Employee";
	}

	@DeleteMapping("/{employeeId}")
	// @PreAuthorize("hasAuthority('DELETE_AUTHORITY')") --> YOU CAN USE AUTHORITIES
	public String deleteAdminEmployee( @PathVariable String employeeId ){
		return "Delete Admin Employee";
	}





}
