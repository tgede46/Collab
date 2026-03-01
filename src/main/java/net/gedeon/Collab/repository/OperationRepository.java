package net.gedeon.Collab.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.gedeon.Collab.entitie.collaboration.Operation;

@Repository
public interface OperationRepository extends JpaRepository<Operation, UUID> {

    List<Operation> findByDocumentIdOrderByServerVersionAsc(UUID documentId);

    List<Operation> findByDocumentIdOrderByAppliedAtDesc(UUID documentId);

    List<Operation> findByDocumentIdAndServerVersionGreaterThan(UUID documentId, long version);
}
