package com.scheduleu.infrastructure.adapter.out.persistence;

import com.scheduleu.domain.model.Salon;
import com.scheduleu.domain.port.out.SalonPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SalonRepository extends JpaRepository<Salon, Integer>, SalonPort {
}
