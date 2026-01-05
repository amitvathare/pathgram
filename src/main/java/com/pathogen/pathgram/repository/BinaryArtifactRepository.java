package com.pathogen.pathgram.repository;

import com.pathogen.pathgram.model.BinaryArtifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BinaryArtifactRepository extends JpaRepository<BinaryArtifact, UUID> {
}

