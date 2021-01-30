package com.egen.ecom.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

@Data
@Entity
@Table(name = "cardDetails")
@EntityListeners(AuditingEntityListener.class)
public class CardDetail {
	@Id
	@GeneratedValue
	@Column
	private long id;
	private String country;
	private int expiredMonth;
	private int expiredYear;
	private String fingerprint;
	private String lastFourDigit;
	private String network;

	@CreatedDate
	@Temporal(TemporalType.DATE)
	@Column(name = "CreatedDate", updatable = false)
	private Date createdDate;

	@LastModifiedDate
	@Temporal(TemporalType.DATE)
	@Column(name = "UpdatedDate")
	private Date updatedDate;
}
