package com.example.javaapp.inputadapter;

import com.example.javaapp.core.IGetThingsDone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppRestController {

    @Autowired
    private IGetThingsDone iGetThingsDone;

    @PostMapping("/items")
    public void createItem() {
    }
}
