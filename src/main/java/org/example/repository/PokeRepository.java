package org.example.repository;

import org.example.entity.Poke;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PokeRepository extends JpaRepository<Poke, Long> {

    List<Poke> findByReceiverOrderByCreatedAtDesc(User receiver);

    void deleteBySender(User user);

    void deleteByReceiver(User user);
}
