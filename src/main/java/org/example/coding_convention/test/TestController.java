package org.example.coding_convention.test;

import org.example.coding_convention.common.response.BaseResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    public BaseResponse<String> test(){
        return BaseResponse.success("test5");
    }

    @GetMapping("/health")
    public BaseResponse<String> health() {
        return BaseResponse.success("health");
    }

}
