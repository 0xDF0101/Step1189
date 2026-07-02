package org.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class GroupPageController {

    @GetMapping("/groups")
    public String groupList() {
        return "groups";
    }

    @GetMapping("/groups/{id}")
    public String groupDetail(@PathVariable Long id) {
        return "group-detail";
    }
}
