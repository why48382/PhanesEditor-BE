package org.example.coding_convention.code_run.controller;

import org.example.coding_convention.common.model.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/code")
public class CodeController {

    @GetMapping("/run")
    public BaseResponse<String> run() {
        return BaseResponse.success("입력 확인");
    }

//    test analyze
}