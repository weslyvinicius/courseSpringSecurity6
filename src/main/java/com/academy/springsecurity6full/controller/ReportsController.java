package com.academy.springsecurity6full.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @GetMapping
    public String getAllReports() {
        return "Get all reports";
    }

    @GetMapping("/{reportId}")
    public String getReport(@PathVariable String reportId) {
        return "Read Report: " + reportId;
    }

    @PostMapping
    public String createReport() {
        return "Create Report";
    }

    @PutMapping("/{reportId}")
    public String updateReport(@PathVariable String reportId) {
        return "Update Report: " + reportId;
    }

    @DeleteMapping("/{reportId}")
    public String deleteReport(@PathVariable String reportId) {
        return "Delete Report: " + reportId;
    }

    @GetMapping("/combined-auth")
    public String getReportWithCombinedAuth() {
        return "This endpoint requires both READ_REPORT authority and MANAGER role";
    }
}
