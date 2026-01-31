package it.medcare.profiling.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    private String nome;
    private String cognome;
    private String citta;
    private String indirizzo;
    private String codiceFiscale;
    private String telefono;
    private String username;
    private String email;
    private String password;
    
    @ManyToOne
    @JoinColumn(name = "role_id")
    private MdtRole role;
    private String typeDoctor;
    
    private Boolean isInternal;
    private Boolean isMed;
    private Byte flagDeleted;
    
    //Getter e Setter
    
    
	public Long getUserId() {
		return userId;
	}
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getCognome() {
		return cognome;
	}
	public void setCognome(String cognome) {
		this.cognome = cognome;
	}
	public String getCitta() {
		return citta;
	}
	public void setCitta(String citta) {
		this.citta = citta;
	}
	public String getIndirizzo() {
		return indirizzo;
	}
	public void setIndirizzo(String indirizzo) {
		this.indirizzo = indirizzo;
	}
	public String getCodiceFiscale() {
		return codiceFiscale;
	}
	public void setCodiceFiscale(String codiceFiscale) {
		this.codiceFiscale = codiceFiscale;
	}
	public String getTelefono() {
		return telefono;
	}
	public void setTelefono(String telefono) {
		this.telefono = telefono;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public MdtRole getRole() {
		return role;
	}
	public void setRole(MdtRole role) {
		this.role = role;
	}
	public String getTypeDoctor() {
		return typeDoctor;
	}
	public void setTypeDoctor(String typeDoctor) {
		this.typeDoctor = typeDoctor;
	}
	public Boolean getIsInternal() {
		return isInternal;
	}
	public void setIsInternal(Boolean isInternal) {
		this.isInternal = isInternal;
	}
	public Boolean getIsMed() {
		return isMed;
	}
	public void setIsMed(Boolean isMed) {
		this.isMed = isMed;
	}
	public Byte getFlagDeleted() {
		return flagDeleted;
	}
	public void setFlagDeleted(Byte flagDeleted) {
		this.flagDeleted = flagDeleted;
	}

}
