import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-alarm-report',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './alarm-report.component.html',
  styleUrl: './alarm-report.component.css'
})
export class AlarmReportComponent {
  fromDate: string = '';
  toDate: string = '';
  errorMessage: string = '';
  private apiBaseUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient) {}

  downloadAlarmReport() {
    this.errorMessage = '';

    if (!this.isValidDateRange()) {
      this.errorMessage = 'Please select both From Date and To Date.';
      return;
    }

    if (!this.isToDateAfterFromDate()) {
      this.errorMessage = 'To Date must be after From Date.';
      return;
    }

    const formattedFrom = this.formatDateForSQL(this.fromDate);
    const formattedTo = this.formatDateForSQL(this.toDate);
    const downloadUrl = `${this.apiBaseUrl}/alarm-report/download?startDate=${encodeURIComponent(formattedFrom)}&endDate=${encodeURIComponent(formattedTo)}`;
    window.open(downloadUrl, '_blank');
  }

  formatDateForSQL(date: string): string {
    return date.replace('T', ' ') + ':00';
  }

  isValidDateRange(): boolean {
    return this.fromDate !== '' && this.toDate !== '';
  }

  isToDateAfterFromDate(): boolean {
    return new Date(this.toDate) > new Date(this.fromDate);
  }

  validateYear(dateString: string, field: 'from' | 'to') {
    const date = new Date(dateString);
    const year = date.getFullYear();

    if (year < 1000 || year > 9999) {
      alert(`${field === 'from' ? 'From Date' : 'To Date'} must have a 4-digit year.`);
      if (field === 'from') this.fromDate = '';
      if (field === 'to') this.toDate = '';
    }
  }
}
