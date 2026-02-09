package org.example.bible;

import org.example.entity.Bible;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BibleRepository extends JpaRepository<Bible, Integer> {

}
