package com.cos.security1.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// DB가 만들어짐
@Entity
@Data
@NoArgsConstructor
public class User {
	@Id		//primary key 직접 할당
	@GeneratedValue(strategy = GenerationType.IDENTITY)	//auto_increment MySQL한테 자동생성 위임.
	private int id;
	private String username;
	private String password;
	private String email;
	private String role;	
	
	private String provider;
	private String providerId;
	
	@CreationTimestamp
	private Timestamp createDate;

	@Builder
	public User(String username, String password, String email, String role, String provider, String providerId,
			Timestamp createDate) {
		this.username = username;
		this.password = password;
		this.email = email;
		this.role = role;
		this.provider = provider;
		this.providerId = providerId;
		this.createDate = createDate;
	}
}
