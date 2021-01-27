package com.egen.ecom.repository;
import java.util.Optional;

import com.egen.ecom.model.Item;
import com.egen.ecom.model.ItemID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, ItemID> {
	Optional<Item> findByItemID(ItemID itemID);
}
