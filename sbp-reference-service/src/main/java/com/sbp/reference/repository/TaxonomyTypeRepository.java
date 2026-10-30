package com.sbp.reference.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sbp.reference.entity.TaxonomyType;

public interface TaxonomyTypeRepository extends JpaRepository<TaxonomyType, String> {

	List<TaxonomyType> findByActiveTrueOrderBySortOrderAscCodeAsc();

}
