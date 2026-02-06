package com.investments.tracker.infrastructure.persistence.repository;

import com.investments.tracker.infrastructure.persistence.entity.InstrumentJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentJpaRepository extends JpaRepository<InstrumentJpaEntity, String> {
}
