package com.egen.ecom.model;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.egen.ecom.enums.DeliveryTypeEnum;
import com.egen.ecom.enums.OrderStatusEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "Order")
@EntityListeners(AuditingEntityListener.class)
public class Order {

	@Id
	@Column(unique = true)
	private Long orderID;

	private UUID customerId;

	@OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	public List<Item> items = new ArrayList<>();

	@OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Payment> payments = new HashSet<>();

	@Column(name = "CreateDate", updatable = false)
	@Temporal(TemporalType.DATE)
	@CreatedDate
	private Date orderedAt;

	@Column(name = "UpdatedDate")
	@Temporal(TemporalType.DATE)
	@LastModifiedDate
	private Date updatedAt;

	@Enumerated(EnumType.STRING)
	private OrderStatusEnum orderStatus;

	private Double orderSubtotal;

	private Double tax_fee;

	private Double total;

	@Enumerated(EnumType.STRING)
	private DeliveryTypeEnum delivery_method;

	@ManyToOne(optional = false)
	@JoinColumn
	private Address order_shipping_address;

	@ManyToOne(optional = false)
	@JoinColumn
	private Address order_billing_address;

	@PreUpdate
	@PrePersist
	public void calcTotal() {
		DecimalFormat df = new DecimalFormat("00.00");
		if (this.items != null && !this.items.isEmpty()) {
			this.orderSubtotal = Double
					.parseDouble(df.format(this.items.stream().mapToDouble(item ->item.getItemCost()).sum()));
			this.total = Double.parseDouble(df.format((1 + (this.tax_fee / 100)) * this.orderSubtotal));
		} else {
			this.orderSubtotal = 0.0;
			this.total = 0.0;
		}

	}
}
