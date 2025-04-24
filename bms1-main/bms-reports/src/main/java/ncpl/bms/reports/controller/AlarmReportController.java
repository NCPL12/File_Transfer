package ncpl.bms.reports.controller;

import ncpl.bms.reports.service.AlarmReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/v1/alarm-report")
@CrossOrigin(origins = "http://localhost:4200")
public class AlarmReportController {

    @Autowired
    private AlarmReportService alarmService;

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadAlarmReport(@RequestParam String startDate,
                                                      @RequestParam String endDate) {
        try {
            // Convert yyyy-MM-dd HH:mm:ss → epoch milliseconds
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long startMillis = sdf.parse(startDate).getTime();
            long endMillis = sdf.parse(endDate).getTime();

            byte[] pdf = alarmService.generateAlarmReportPdf(startMillis, endMillis);

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=alarm_report.pdf")
                    .header("Content-Type", "application/pdf")
                    .body(pdf);

        } catch (Exception e) {
            e.printStackTrace(); // log the error
            return ResponseEntity.internalServerError().body(null);
        }
    }
}
