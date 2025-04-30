package com.nailic.JwtAuth.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("any")
public class ApiController {
  @GetMapping("api")
  private String api() {
    return "api";
  }
}
