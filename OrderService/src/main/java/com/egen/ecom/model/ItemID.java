package com.egen.ecom.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class ItemID implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1625391237724695470L;
	private Long orderid;

	@Column
	private String skuID;
}
