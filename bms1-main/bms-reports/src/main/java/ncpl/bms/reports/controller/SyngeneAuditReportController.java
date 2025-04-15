package ncpl.bms.reports.controller;

import ncpl.bms.reports.service.SyngeneAuditReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/v1/audit-report")
@CrossOrigin(origins = "http://localhost:4200")
public class SyngeneAuditReportController {

    @Autowired
    private SyngeneAuditReportService auditService;

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadAuditReport(@RequestParam String startDate,
                                                      @RequestParam String endDate) {
        byte[] pdf = auditService.generateAuditReportPdf(startDate, endDate);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=syngene_audit_report.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }

}
