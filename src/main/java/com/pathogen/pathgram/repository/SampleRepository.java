package com.pathogen.pathgram.repository;

import com.pathogen.pathgram.model.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SampleRepository extends JpaRepository<Sample, UUID> {
}

