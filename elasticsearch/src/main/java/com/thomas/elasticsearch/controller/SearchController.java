package com.thomas.elasticsearch.controller;

import com.thomas.elasticsearch.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: lf
 * @Date: 2019/5/24 9:37
 */
@RestController
public class SearchController {
    @Autowired
    private BookService bookService;
    @GetMapping("/get/book/novel")
    public ResponseEntity get(){
            return null;
    }

}
