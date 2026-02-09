package org.example.bible;

import lombok.RequiredArgsConstructor;
import org.example.entity.Bible;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class BibleController {

    private final BibleService bibleService;

    @GetMapping("/bibles/{bibleId}")
    ResponseEntity<Bible> getBible(@PathVariable int bibleId) {
        if(bibleId < 1 || bibleId > 66) {
            throw new IllegalArgumentException();
            /** TODO
             * 커스텀 예외처리 하던가 이런 검증을
             * service에서 해야할지, controller에서 해야할지 고민하기
             */
        }

        Bible bible = bibleService.getBibleInfo(bibleId);

        return ResponseEntity.ok().body(bible);
    }

//    @GetMapping("/bibles")
//    ResponseEntity<> getBibles() {
//
//    }

}
