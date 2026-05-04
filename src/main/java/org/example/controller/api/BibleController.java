package org.example.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dto.user.CustomUserDetails;
import org.example.service.BibleService;
import org.example.service.JpaProgressServiceImpl;
import org.example.entity.Bible;
import org.example.utility.annotation.LoginUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BibleController {

    private final BibleService bibleService;
    private final JpaProgressServiceImpl jpaProgressServiceImpl;

    /**
     *  사실상 얘도 호출되는 애는 아님
     */
    @ResponseBody
    @GetMapping("/bibles/{bibleId}")
    ResponseEntity<Bible> getBible(@PathVariable int bibleId) {
        if(bibleId < 1 || bibleId > 66) {
            // TODO 예외처리 똑바로 하기
            throw new IllegalArgumentException();
        }

        Bible bible = bibleService.getBibleInfo(bibleId);

        return ResponseEntity.ok().body(bible);
    }

    @ModelAttribute("bibles") // 별도 로직 없이 캐싱된 bible 데이터를 꺼내쓸 수 있음
    public List<Bible> bibles() {
        return bibleService.getAllBibles();
    }

    /**
     * 일단은 이게 최선인 듯 ?
     */
    @GetMapping("/bibles")
    public String getBibles(
            @LoginUser Long userId,
            Model model
            ) {

        model.addAttribute("bibles", bibleService.getAllBibles());
        model.addAttribute("userProgress", jpaProgressServiceImpl.getAllProgress(userId));

        return "bible-list";
    }

}
