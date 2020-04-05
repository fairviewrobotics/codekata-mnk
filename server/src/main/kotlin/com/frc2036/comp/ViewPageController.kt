package com.frc2036.comp

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

// Controller to show the page that displays graphically the state of the competition
@Controller
class ViewPageController {
    @RequestMapping("/")
    fun main() = "index.html"

    @RequestMapping("/admin")
    fun admin(@RequestParam key: String): String {
        val correctKey = System.getenv("MNK_ADMIN_KEY") ?: "adminkey"
        return if(key != correctKey) "invalidAdminKey.html" else "admin.html"
    }
}