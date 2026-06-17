package com.releasescribe.repository;

import com.releasescribe.model.ReleaseNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReleaseNoteRepository extends JpaRepository<ReleaseNote, UUID> {

    List<ReleaseNote> findAllByOrderByCreatedAtDesc();
}
