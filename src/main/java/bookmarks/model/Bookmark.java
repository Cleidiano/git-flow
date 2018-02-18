package bookmarks.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Bookmark {
	
	@Id
	@GeneratedValue
	private Long id;
	
	@ManyToOne
	@JsonIgnore
	private Account account; 
	
	private String uri;
	
	private String description;
	
	private Bookmark() {} //JPA
	
	public Bookmark(Account account, String uri, String description) {
		this.account = account;
		this.uri = uri;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public Account getAccount() {
		return account;
	}

	public String getUri() {
		return uri;
	}

	public String getDescription() {
		return description;
	}
	
	
	
	
}
