package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.repository.BibleRepository;
import org.example.entity.Bible;
import org.example.exception.EntityNotFoundException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BibleService implements ApplicationRunner {

    private final BibleRepository bibleRepository;
    private volatile List<Bible> cachedBibles; // 서버가 실행될때 한번만 bible 데이터를 가져옴

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 서버가 완전히 준비된 후 실행되므로 data.sql 데이터가 보장됨!
        List<Bible> list = bibleRepository.findAll();
        log.info("✅ 성경 데이터 캐싱 완료");
        this.cachedBibles = List.copyOf(list); // ---> 불변 리스트로 저장해야함!
    }

    public List<Bible> getAllBibles() {
        if(cachedBibles == null) {
            throw new IllegalStateException("성경 데이터가 초기화되지 않았습니다.");
        }

        return cachedBibles;
    }

    public Bible getBibleInfo(int bibleId) {
        if(cachedBibles == null) {
            throw new IllegalStateException("성경 데이터가 초기화되지 않았습니다.");
        }

        return cachedBibles.stream()
                .filter(b -> b.getId() == bibleId)
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("해당 성경을 찾을 수 없습니다."));
    }
}
