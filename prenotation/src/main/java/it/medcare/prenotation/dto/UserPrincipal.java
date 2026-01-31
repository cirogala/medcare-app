package it.medcare.prenotation.dto;

public class UserPrincipal {

	private Long id;
	private String username;

	public UserPrincipal(Long id, String username) {
		this.id = id;
		this.username = username;
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}
}