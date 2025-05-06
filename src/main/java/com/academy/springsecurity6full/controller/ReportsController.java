package com.academy.springsecurity6full.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
public class ReportsController {

    @GetMapping
    @PreAuthorize("hasAuthority('READ_REPORT')")
    public String getAllReports() {
        return "Get all reports";
    }

    @GetMapping("/{reportId}")
    @PreAuthorize("hasAuthority('READ_REPORT')")
    public String getReport(@PathVariable String reportId) {
        return "Read Report: " + reportId;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_REPORT')")
    public String createReport() {
        return "Create Report";
    }

    @PutMapping("/{reportId}")
    @PreAuthorize("hasAuthority('UPDATE_REPORT')")
    public String updateReport(@PathVariable String reportId) {
        return "Update Report: " + reportId;
    }

    @DeleteMapping("/{reportId}")
    @PreAuthorize("hasAuthority('DELETE_REPORT')")
    public String deleteReport(@PathVariable String reportId) {
        return "Delete Report: " + reportId;
    }

    @GetMapping("/combined-auth")
    @PreAuthorize("hasAuthority('READ_REPORT') and hasRole('MANAGER')")
    public String getReportWithCombinedAuth() {
        return "This endpoint requires both READ_REPORT authority and MANAGER role";
    }
}
